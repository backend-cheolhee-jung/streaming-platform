# Gateway Security 정비 + Docker Compose 통합 + 모듈 README 정비 설계서

작성일: 2026-05-03
작성자: Claude (자율 모드)

## 1. 목적

video-platform 마이크로서비스 모노레포에서 다음을 한 번에 달성한다.

1. 모든 모듈에 표준화된 README 추가 — 각 모듈의 역할, 언어/프레임워크, 핵심 워크플로우를 한눈에 파악할 수 있게 한다.
2. **Gateway**가 단일 인증·인가 진입점이 되도록 Spring Security 설정과 라우팅을 정비한다. 다운스트림 서비스는 인증 책임을 갖지 않고, gateway가 검증한 `user-id` 헤더만 신뢰한다.
3. `docker compose up` 으로 인프라(postgres/redis/mongo/kafka/axon-server)와 애플리케이션이 한 네트워크에서 통신할 수 있도록 compose 와 각 모듈의 설정을 정리한다.
4. Gateway에 단위/통합 테스트를 추가하고, gateway → user 흐름에 대한 E2E 동작을 실 호출로 검증한다.
5. 변경사항은 의미 단위로 작은 커밋으로 쪼개고, 단일 PR로 main 에 올린다.

## 2. 발견된 핵심 결함

탐색 단계에서 다음의 실질적 결함을 확인했다 (변경 근거).

### 2.1 Gateway

- `JwtAuthenticationGlobalFilter`
  - `tokenDecoder.decode(exchange.accessToken)` 을 무조건 호출 — `accessToken` extension은 헤더가 없으면 `IllegalArgumentException` 을 던진다. 따라서 토큰이 필요 없는 `/users/login`, `/users/signup` 등이 즉시 깨진다.
  - `SecurityContextHolder.getContext()` 사용 — Spring WebFlux 는 `ReactiveSecurityContextHolder` 와 `Context` 전파를 사용해야 한다. 현재 코드는 reactive 환경에서 동작하지 않는다.
- `SecurityConfig`
  - `cors{ it.configurationSource { corsConfigurationSource() } }` 호출 형태가 reactive `ServerHttpSecurity.CorsSpec` 시그니처와 맞지 않는다 (`CorsConfigurationSource` 가 들어가야 함).
  - JWT 검증을 `WebFilter` 가 수행하는 것 자체는 OK 이지만, 토큰 부재시 익명 인증으로 흘려보내는 분기와, public/secured 매처가 service-by-service 로 분산되어 가독성/관리성이 떨어진다.
- `Router.extractAndAddUserIdToHeader`
  - `exchange.request.userId = …` 로 setter 를 호출하는데, 이 setter 는 `mutate().header(...).build()` 한 새 request 를 반환만 하고 버린다 → 다운스트림에 `user-id` 헤더가 실제로 전달되지 않는다.
  - `setPath(USER_DOMAIN)` 은 패턴 문자열 `/users/**` 자체를 path 로 설정하려 한다 — 잘못된 사용. `rewritePath` 도 `/api/user/...` 패턴으로 다시 prefix 를 바꾸려 하지만 path matcher 와 일치하지 않는다.
- JWT 포맷
  - User 모듈은 auth0 JWT 라이브러리로 `user-id`(long), `username`(string), `role`(string) 클레임을 발행한다.
  - Gateway 는 `subject.toLong()` 로 사용자 ID 를 읽고, `roles`(쉼표 join) 클레임을 읽는다.
  - **두 모듈 사이에서 JWT 가 절대 통과되지 않는다.**
- 의존성: `spring-security-jwt:1.1.1.RELEASE` 는 더 이상 필요 없는 레거시 — 제거.

### 2.2 User 모듈 JWT 발행 결함

- `JwtManager.createToken` 의 `roles.joinToString { "," }` 는 `transform` 람다이므로 항상 `","` 만 반환 → 결과적으로 role 클레임은 `","` 로 join 된 콤마 문자열이 된다. (예: 두 역할 → `,,` )
- 이로 인해 어떤 클라이언트도 role 정보를 사용할 수 없다.

### 2.3 Docker Compose / 네트워킹

- 일부 서비스만 `cherhy-network` 에 join 됨. postgres/mongo/redis/gateway/payment/stream/user/schedule 는 네트워크 미지정 → 기본 default 네트워크를 사용하게 되지만, kafka-ui, axon-server 만 cherhy-network 에 있어 의도가 흐트러짐.
- 포트 매핑: `gateway: "10100:8080"`, `payment: "10101:8080"`, `user: "10103:8080"` — 컨테이너 내부 포트는 각 앱 설정의 실제 포트(10100/10101/10103)이지 8080 이 아니다. 매핑이 깨져있어 호스트에서 접근 불가.
- Gateway `application.yml` 의 다운스트림 URL 이 `http://localhost:10101` 등으로 박혀 있어 컨테이너 내부에서는 해석 불가.
- `payment.depends_on: gateway` 처럼 의존성이 거꾸로 (gateway 는 다운스트림 모두에 의존하지 않아도 되지만, 이렇게 두면 순환은 없어도 의도가 부적절).
- compose 의 `axonserver-data` 등 named volume + bind mount 혼용이 잘못 — bind mount 면 그냥 volumes 섹션에서 host path 를 직접 매핑.

### 2.4 모듈 README

- `user/README.MD`, `stream/README.MD`, `schedule/README.MD` 만 존재하고 매우 간소.
- gateway, payment, common, consumer, producer 는 README 자체가 없다.

## 3. 범위 (YAGNI)

이번 작업에서 한다:

- Gateway: Spring Security + 라우팅 + JWT 디코더 + 테스트를 깔끔히 다시 정리.
- User 모듈: gateway 가 신뢰하는 JWT 포맷에 맞춰 발행 결함 수정. (아래 4.2)
- Docker Compose: 네트워크/포트/depends_on/bind volume 정리. gateway 의 다운스트림 URL 을 docker 프로파일 분리로 처리.
- 8개 모듈 모두에 표준 형식의 README 추가/갱신.
- E2E: gateway → user 의 회원가입 → 로그인 → /users/me 흐름을 docker compose 위에서 실제 호출로 검증. (payment/stream 은 인프라 의존성이 크고 본 작업 범위 밖이므로 E2E 제외, but compose 자체는 부팅 가능하도록 정리.)
- 작은 단위 커밋(타입별: docs / build / fix / test / chore) → 단일 feature branch → main 으로 PR.

이번 작업에서 **하지 않는다**:

- payment/stream 도메인 로직 변경.
- Kafka 토픽 추가 / Axon aggregate 변경.
- restdocs 도입.
- 그 외 ROOT README TODO 항목.

## 4. 설계

### 4.1 Gateway 인증/인가 아키텍처

```
Client ──HTTP──▶ Gateway (Spring Cloud Gateway, WebFlux, Spring Security)
                  │
                  │ 1. SecurityWebFilterChain
                  │    - public matcher: /users/login, /users/signup, /users/refresh, /actuator/health
                  │    - 이외 전부: ServerAuthenticationConverter (JwtTokenAuthenticationConverter) 로
                  │      Authorization: Bearer <jwt> 추출 → ReactiveAuthenticationManager 가 검증
                  │    - 인증 성공: ReactiveSecurityContext 에 Authentication(principal=GatewayUserPrincipal) 저장
                  │    - 인증 실패: 401, 또는 (public 매처면 통과)
                  │
                  │ 2. UserContextGatewayFilter (GlobalFilter, order = -1)
                  │    - SecurityContext 에 인증이 있으면 principal.userId 를
                  │      "user-id" 헤더로 추가하여 mutate된 exchange 를 다음 필터로 전달
                  │
                  │ 3. RouteLocator
                  │    - /users/**   → http://user:10103
                  │    - /payments/** → http://payment:10101
                  │    - /streams/** → http://stream:10102
                  │    (path rewriting 없음. gateway 와 다운스트림이 동일한 path 사용.)
```

핵심 단위:

- `JwtTokenAuthenticationConverter (ServerAuthenticationConverter)` — `Authorization: Bearer ...` 헤더에서 토큰을 꺼내 `JwtAuthenticationToken(token)` 로 변환. 토큰이 없으면 `Mono.empty()` (인증 시도 안 함).
- `JwtReactiveAuthenticationManager (ReactiveAuthenticationManager)` — 주입받은 `TokenDecoder` 로 토큰을 검증/파싱하여 `Authentication(principal=GatewayUserPrincipal, authorities=roles)` 반환. 실패 시 `BadCredentialsException`.
- `TokenDecoder` — Nimbus `MACVerifier` 로 HMAC 검증 + claims 파싱. **User 모듈 JWT 포맷에 맞춰** `user-id` (long, **숫자**로 발행되므로 `getLongClaim("user-id")` 사용), `username` (string), `role` (string, 쉼표 분리) 클레임을 읽는다. `subject` 는 더이상 사용하지 않는다. 만료(`exp`) 도 검증한다 (`expirationTime ?: now-1` 비교).
- `GatewayUserPrincipal` — `userId: Long`, `username: String`, `roles: List<String>` 보유. (기존 `Principal` 을 이름만 바꿔 책임 명확화.)
- `UserContextGatewayFilter (GlobalFilter)` — Reactive Security Context 에서 `Authentication` 을 꺼내 principal 의 `userId` 를 `user-id` 헤더로 mutated request 에 붙여 다음 체인으로 넘긴다. **mutate 결과를 chain 에 전달하는 부분**이 기존 버그를 고치는 포인트.
- `SecurityConfig`
  - `passwordEncoder` 빈은 gateway 에 필요 없으므로 제거.
  - `securityFilterChain` 에서 CORS/CSRF/AuthMatchers/AuthenticationManager/AuthenticationConverter 를 모두 wire.
  - public path matcher 한 곳에 모음:
    - `POST /users/signup`, `POST /users/login`, `POST /users/refresh`, `GET /actuator/health`
  - 그 외는 모두 `authenticated()`.
  - **주의 (out of scope)**: 현재 user 모듈의 `/users/refresh` 는 refresh-token 쿠키 검증 없이 `user-id` 헤더만 신뢰한다. 본 작업에서는 이를 그대로 두고 public 으로 노출만 한다. 정상적인 refresh 플로우 보강은 후속 작업.
- `Router (RouteLocator)`
  - 다운스트림 URL 을 `DomainProperty` 에서 주입.
  - `path("/users/**")` → `domainProperty.userUrl`. 별도 rewrite 없음.
  - 기존 `extractAndAddUserIdToHeader` 라우트 단계 필터 제거 — 글로벌 필터 `UserContextGatewayFilter` 가 일괄 처리.

JWT 알고리즘 정합성:
- User 모듈은 `Algorithm.HMAC256(secret)` (auth0) 사용 → 결과 JWS는 HS256.
- Gateway 는 Nimbus `MACVerifier(SecretKeySpec(secret, "HmacSHA256"))` 으로 HS256 검증 가능.
- **secret 은 동일 값 필요** — `secret` 으로 통일. (현재 양쪽 다 `"secret"` 이지만 명시적으로 두 곳의 application 설정에서 일치해야 함을 README/주석으로 박아둠.)

### 4.2 User 모듈 JWT 발행 정합화

- `JwtManager.createToken` 의 role join 버그 수정:
  - `roles.joinToString { "," }` → `roles.joinToString(separator = ",") { it.name }` (또는 `name` 프로퍼티가 있는 경우).
- 클레임 키는 그대로 (`user-id`, `username`, `role`) — gateway 가 이 포맷에 맞춤.
- audience/issuer 는 영향 없으므로 손대지 않음.

### 4.3 Docker Compose 정비

- 모든 서비스에 `networks: [cherhy-network]` 명시. 
- 호스트 ↔ 컨테이너 포트 매핑을 동일 포트로 정렬:
  - gateway `10100:10100`, payment `10101:10101`, stream `10102:10102`, user `10103:10103`, schedule `10104:10104`.
- Gateway 의 `application.yml` 을 docker 환경용 프로파일과 분리:
  - `application.yml` — 로컬 default (localhost 기반).
  - `application-docker.yml` — 컨테이너용 (`http://user:10103` 등).
  - Dockerfile 의 `SPRING_PROFILES_ACTIVE` 를 `dev` → `docker` 로 변경.
- `depends_on` 정리: payment/stream/user/schedule 은 인프라(postgres-master/slave, mongo, redis, axon-server) 에만 의존. gateway 는 user/payment/stream 에 의존(시작 순서를 위해).
- volumes 섹션의 named-volume + bind 혼용은 단순 host bind 로 정리 (named volume 블록 삭제, 각 서비스의 `volumes:` 에서 host path 직접 bind).
- `init.sql` 은 그대로 사용 — DB `user`, `payment`, `stream`, `schedule`, `cherhy` 가 자동 생성됨.

E2E 부팅에 꼭 필요한 서비스 핵심 셋: `postgres-master-container`, `postgres-slave-container`, `user`, `gateway`. (payment/stream 은 axon-server/mongodb/redis/kafka 모두 필요해 부팅 비용이 크다. 따라서 본 작업의 E2E 검증은 user 도메인 한정. compose 자체는 모두 정의되어 있고 빌드/구동 가능한 상태.)

### 4.4 모듈 README 표준 포맷

각 모듈의 `README.md` 를 다음 섹션으로 통일한다.

```
# <Module Name>

## 역할
이 모듈이 해결하는 문제 1-3 줄.

## 언어/프레임워크
- Language: Kotlin x.y / JDK 21
- Framework: Spring Boot 3.2.5 / Ktor 2.3.11 / ...
- 핵심 라이브러리: ...

## 워크플로우 요약
- 진입점: ...
- 외부 의존성: postgres-master, redis, axon-server, ...
- 발행하는 이벤트 / 소비하는 이벤트: ...
- 인증: gateway 에서 검증한 `user-id` 헤더 신뢰

## 로컬 실행
명령 한두 줄.
```

다음 모듈에 작성한다: `gateway`, `user`, `payment`, `stream`, `schedule`, `consumer`, `producer`, `common`. 기존 `user/README.MD`, `stream/README.MD`, `schedule/README.MD` 는 신규 표준에 맞춰 갱신 (파일 이름은 `README.md` 로 통일 — case-insensitive FS에서 충돌 없도록 `git mv` 사용).

### 4.5 테스트 전략

Gateway:

- Unit
  - `TokenDecoderTest`: 유효 토큰 디코딩, 만료 토큰 처리, 잘못된 서명 처리, 클레임 누락 처리.
  - `JwtTokenAuthenticationConverterTest`: 헤더 없을 때 `Mono.empty()`, Bearer 가 아닐 때 empty, 정상 헤더에서 토큰 토큰화.
  - `UserContextGatewayFilterTest`: 인증 있을 때 `user-id` 헤더 추가, 없을 때 그대로 통과.
- Slice/Integration (`@SpringBootTest(webEnvironment = RANDOM_PORT)` 또는 `WebTestClient` 기반)
  - 라우팅 테스트: gateway 가 mock-server (간단한 ad-hoc reactor server) 로 정확한 path/heaer 를 forwarding 하는지 검증.
  - SecurityFilterChain 테스트: public 경로는 통과, secured 경로는 401, 유효 JWT 시 200 + `user-id` 헤더가 다운스트림에 도달.

E2E (수동, 기록 안 남김):

- `./gradlew :user:shadowJar :gateway:bootJar` 로 jar 빌드.
- `docker compose up -d postgres-master-container postgres-slave-container user gateway` (필요 인프라만).
- curl 시나리오:
  1. `POST http://localhost:10100/users/signup` → 201
  2. `POST http://localhost:10100/users/login` → 201 + Authorization 응답 헤더에 access token
  3. `GET http://localhost:10100/users/me` (Bearer) → 200 + 본인 정보
  4. 토큰 없이 `GET http://localhost:10100/users/me` → 401
  5. 토큰 없이 `GET http://localhost:10100/users/login` (실수 호출 가정) → 405/200 (적어도 401 아님)
- 결과 정상 확인 후 `docker compose down`.

### 4.6 커밋/PR 분할

브랜치: `feature/gateway-security-and-readme`

커밋 (작은 단위, 타입 prefix):

1. `docs: add module READMEs across all 8 modules` — 모든 README 신규/갱신.
2. `chore(compose): unify network, ports, depends_on for module communication` — compose.yaml 정리.
3. `fix(user): correct role claim joinToString in JwtManager` — user 모듈 JWT 결함.
4. `refactor(gateway): introduce reactive JWT auth chain` — `TokenDecoder` 정비, `JwtTokenAuthenticationConverter`/`JwtReactiveAuthenticationManager` 신설, `JwtAuthenticationGlobalFilter` 제거.
5. `refactor(gateway): replace setPath/rewritePath with header-injecting global filter` — `UserContextGatewayFilter` 신설, `Router` 단순화.
6. `chore(gateway): add docker profile config and align dockerfile` — `application-docker.yml` 추가.
7. `test(gateway): add unit and integration tests for security & routing` — 테스트 코드.
8. `docs(gateway): document gateway responsibilities and JWT contract` — gateway README 보강 (1번 커밋 보완 가능 시 흡수).

PR 본문에 E2E 시나리오/결과 요약 첨부.

## 5. 위험 및 대응

- **테스트가 실 환경 의존성에 묶이면 fail 함**. → testcontainers/embedded 우선, 부득이한 경우 `WebTestClient` + 임시 reactor server 로 격리.
- **docker compose up 이 호스트 환경에 따라 실패**(예: 포트 점유, postgres bind volume 권한). → E2E 는 user/gateway/postgres 한정으로 좁히고, 실패 시 즉시 로그 첨부 + `docker compose down -v` 후 재시도.
- **roles 클레임 포맷 변경이 기존 발행 토큰을 무효화**할 수 있음. → 영향 범위는 user 모듈 → gateway 흐름뿐. 개발 환경이고 production 토큰이 떠다니지 않으므로 마이그레이션 부담 없음.
- **모노레포의 다른 빌드(`payment`, `stream`)가 깨질 위험**. → 본 변경은 user/gateway/common 외 모듈의 컴파일 단위에 손대지 않음. 전체 `./gradlew build` 로 회귀 확인 (E2E 전 단계).

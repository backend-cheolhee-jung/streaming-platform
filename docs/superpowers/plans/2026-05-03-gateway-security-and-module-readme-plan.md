# Implementation Plan: Gateway Security 정비 + Compose 통합 + 모듈 README

References: `docs/superpowers/specs/2026-05-03-gateway-security-and-module-readme-design.md`

브랜치: `feature/gateway-security-and-readme`

## 작업 순서 (TDD 우선)

각 단계마다 **빨간불 → 초록불 → 리팩터** 사이클을 가능한 한 적용한다.
빌드 영향이 적은 순서(README → compose → user 결함 → gateway 본체 → 테스트 → e2e)로 진행한다.

### Step 1. 모듈 README 표준화 (commit #1)

대상 파일:

- `gateway/README.md` (신규)
- `user/README.md` (기존 `README.MD` 를 `git mv` 후 갱신)
- `payment/README.md` (신규)
- `stream/README.md` (기존 `README.MD` 를 `git mv` 후 갱신)
- `schedule/README.md` (기존 `README.MD` 를 `git mv` 후 갱신)
- `consumer/README.md` (신규)
- `producer/README.md` (신규)
- `common/README.md` (신규)

각각 spec 4.4 의 표준 4-섹션 (역할 / 언어·프레임워크 / 워크플로우 / 로컬 실행) 으로 작성.

**Verification**: `git status` 로 8개 README 변경 확인. 빌드 영향 없음.

**Commit**: `docs: add module READMEs across all 8 modules`.

### Step 2. Docker Compose 정리 (commit #2)

`compose.yaml` 수정:

- 모든 서비스에 `networks: [cherhy-network]` 추가.
- 포트 매핑 동일화: gateway `10100:10100`, payment `10101:10101`, stream `10102:10102`, user `10103:10103`, schedule `10104:10104`.
- `depends_on` 정리: gateway 가 user/payment/stream 에 의존. 다른 서비스의 gateway 의존 제거.
- volumes 섹션의 named volume 블록 삭제. 각 서비스 안의 host bind 만 유지.

**Verification**:
- `docker compose config` 가 에러 없이 파싱되는지.
- 새 추가/제거 검증 (manual diff review).

**Commit**: `chore(compose): unify network, ports, depends_on for module communication`.

### Step 3. User 모듈 JWT 결함 수정 (commit #3)

3.1 `user/src/main/kotlin/cherhy/example/util/JwtManager.kt`:
- `roles.joinToString { "," }` → `roles.joinToString(",") { it.name }` 로 수정.

3.2 신규 테스트: `user/src/test/kotlin/cherhy/example/util/JwtManagerTest.kt`
- 토큰 생성 후 디코드하여 `role` 클레임이 `"UNPAID_MEMBER,..."` 형태가 되는지 검증.
- 기존 `user/src/test/` 가 비어있다면 신규로 만들어 동작 검증.

**Verification**: `./gradlew :user:test` 통과. (auth0/java-jwt 가 user 의존성에 있으므로 사용 가능.)

**Commit**: `fix(user): correct role claim joinToString in JwtManager`.

### Step 4. Gateway 의존성 정리 (commit #4 와 합칠 수도 있음)

4.1 `gateway/build.gradle.kts`:
- `implementation(Security.SPRING_SECURITY_JWT)` 제거 (legacy, 미사용).
- 이미 `SPRING_BOOT_STARTER_SECURITY` + `NIMBUS_JWT` 가 있으므로 그대로 사용.

**Verification**: `./gradlew :gateway:compileKotlin` 통과.

### Step 5. Gateway TDD — JWT 검증 체인 (commit #4)

순서: 테스트 먼저, 실패 확인 후 구현.

5.1 신규 클래스 (이름/위치):
- `gateway/src/main/kotlin/com/cherhy/gateway/security/GatewayUserPrincipal.kt` (기존 `util/model/Principal.kt` 를 옮기고 이름 변경).
- `gateway/src/main/kotlin/com/cherhy/gateway/security/JwtAuthenticationToken.kt` — 인증 전/후 상태를 모두 표현하는 Authentication 구현.
- `gateway/src/main/kotlin/com/cherhy/gateway/security/JwtTokenAuthenticationConverter.kt` — `ServerAuthenticationConverter`.
- `gateway/src/main/kotlin/com/cherhy/gateway/security/JwtReactiveAuthenticationManager.kt` — `ReactiveAuthenticationManager`.
- `gateway/src/main/kotlin/com/cherhy/gateway/security/TokenDecoder.kt` — 기존 `jwt/TokenDecoder.kt` 를 새 패키지로 이동, 시그니처를 `decode(token: String): GatewayUserPrincipal` 로 단순화 (Authentication 으로 감싸는 책임은 manager 가).
- 삭제: `gateway/src/main/kotlin/com/cherhy/gateway/jwt/JwtAuthenticationGlobalFilter.kt`, `jwt/TokenDecoder.kt` 의 옛 위치.

5.2 테스트 파일 (Kotest, 이미 의존성에 있음):
- `gateway/src/test/kotlin/com/cherhy/gateway/security/TokenDecoderTest.kt` — Nimbus 로 직접 토큰 발행 후 decode → principal 검증, 만료/서명 변조 케이스.
- `gateway/src/test/kotlin/com/cherhy/gateway/security/JwtTokenAuthenticationConverterTest.kt` — `MockServerHttpRequest` / `MockServerWebExchange` 로 헤더 유무 케이스.
- `gateway/src/test/kotlin/com/cherhy/gateway/security/JwtReactiveAuthenticationManagerTest.kt` — 토큰 검증 성공/실패.

**Verification**: `./gradlew :gateway:test --tests "*security*"`.

**Commit**: `refactor(gateway): introduce reactive JWT auth chain`.

### Step 6. Gateway TDD — 사용자 컨텍스트 헤더 주입 + 라우팅 정비 (commit #5)

6.1 신규 클래스:
- `gateway/src/main/kotlin/com/cherhy/gateway/filter/UserContextGatewayFilter.kt` — `GlobalFilter`. ReactiveSecurityContext 에서 인증 꺼내 `user-id` 헤더 mutate, **mutated request 를 chain.filter 에 전달**.
- `gateway/src/main/kotlin/com/cherhy/gateway/config/SecurityConfig.kt` 재작성:
  - `passwordEncoder` 빈 제거.
  - `securityFilterChain` 에서 cors/csrf/authenticationManager/authenticationConverter 결합.
  - public matcher 일괄 정리.
- `gateway/src/main/kotlin/com/cherhy/gateway/router/Router.kt` 단순화:
  - `path("/users/**") → uri(domainProperty.userUrl)`, payment/stream 도 동일.
  - 기존 `extractAndAddUserIdToHeader` 제거.

6.2 테스트:
- `gateway/src/test/kotlin/com/cherhy/gateway/filter/UserContextGatewayFilterTest.kt`
  - 인증 컨텍스트 있을 때 `user-id` 헤더가 추가되는지.
  - 인증 컨텍스트 없을 때 헤더가 추가되지 않는지.
- `gateway/src/test/kotlin/com/cherhy/gateway/config/SecurityConfigIntegrationTest.kt` — `@SpringBootTest` + `WebTestClient` 로 public/secured 매처 검증.
  - `/users/login` POST → 200/permitAll (mock downstream 또는 503 도 OK; status != 401 검증).
  - `/users/me` GET (no token) → 401.
  - `/users/me` GET (valid token) → mock downstream 에서 200, `user-id` 헤더 도달 확인.

**Verification**: `./gradlew :gateway:test`.

**Commit**: `refactor(gateway): replace setPath/rewritePath with header-injecting global filter`.

### Step 7. Docker 프로파일 + Dockerfile 정렬 (commit #6)

7.1 신규: `gateway/src/main/resources/application-docker.yml`
```yaml
domain:
  payment-url: http://payment:10101
  stream-url: http://stream:10102
  user-url: http://user:10103
```

7.2 `gateway/src/main/resources/application.yml` — 로컬 default URL 유지.

7.3 `gateway/Dockerfile` — `ENV SPRING_PROFILES_ACTIVE=docker` 로 변경.

7.4 (선택) payment/user 등의 Dockerfile 도 `dev` → `docker` 통일 가능. 단, payment/stream/user 모듈은 본 작업의 e2e 범위에서 빠지므로 user 만 동기화한다 (`-Dconfig.file` 또는 `-Pconfig` 형태로 ktor 환경변수 사용 — 단 user 의 application.conf 는 host.docker.internal 을 사용하고 있어 docker 네트워크에서도 동작은 한다 (mac/win 한정). Linux 호환은 추후. 이번엔 host.docker.internal 그대로 유지하여 변경 최소화.)

**Verification**: 
- `./gradlew :gateway:bootJar`.
- `SPRING_PROFILES_ACTIVE=docker java -jar gateway/build/libs/gateway.jar` 로 다운스트림 URL 이 `http://user:10103` 으로 잡히는지 로그 확인 (jvm-only 부팅, 실제 호출은 e2e 단계에서).

**Commit**: `chore(gateway): add docker profile config and align dockerfile`.

### Step 8. Gateway README 보강 (commit #7, 합칠 수도 있음)

`gateway/README.md` 를 step 1 에서 작성한 base 위에:
- "JWT contract" 섹션: secret/algorithm/claims 명시.
- "How auth flows": ASCII 다이어그램.
- 로컬 빌드/테스트 명령.

**Commit**: `docs(gateway): document gateway responsibilities and JWT contract`.

### Step 9. 통합 빌드 회귀 + E2E

9.1 모노레포 풀빌드: `./gradlew clean build -x test` (테스트는 모듈별로 이미 통과 확인).
정확히는 `./gradlew :common:build :gateway:build :user:build` 우선. payment/stream/schedule 는 컴파일까지만 OK 상태가 유지되는지 확인.

9.2 E2E:
- `./gradlew :user:shadowJar :gateway:bootJar`
- `docker compose up -d postgres-master-container postgres-slave-container user gateway`
- `docker compose logs -f gateway user` 로 ready 확인 (`Started GatewayApplication` / `Application started`).
- curl 시나리오 (spec 4.5):
  ```
  curl -i -X POST -H 'Content-Type: application/json' \
       -d '{"email":{"value":"a@b.c"},"password":{"value":"pw1234!!"},"confirmPassword":{"value":"pw1234!!"},"name":{"value":"alice"}}' \
       http://localhost:10100/users/signup
  curl -i -X POST -H 'Content-Type: application/json' \
       -d '{"email":{"value":"a@b.c"},"password":{"value":"pw1234!!"}}' \
       http://localhost:10100/users/login
  TOKEN=...
  curl -i -H "Authorization: Bearer $TOKEN" http://localhost:10100/users/me
  curl -i http://localhost:10100/users/me  # expect 401
  ```
- 결과 메모 (PR 본문에 포함).
- `docker compose down`.

**완료 기준**:
- `:gateway:test`, `:user:test` 모두 통과.
- 컴포즈 빌드/부팅 성공.
- 위 4개 curl 시나리오가 의도대로 동작.

### Step 10. PR 생성

```
gh pr create --base main --head feature/gateway-security-and-readme \
  --title "Gateway: align JWT auth, fix routing, add module READMEs" \
  --body "<요약 + 변경점 + e2e 결과>"
```

## 8. Rollback 전략

- 각 커밋이 독립적이므로 문제가 생기면 해당 커밋 단위로 `git revert`.
- compose 변경(2번)이 호스트 이슈를 일으키면 우선 revert 가능. 다른 커밋은 도커와 무관.

## 9. 의존도 그래프 / 병렬화 가능성

본 plan 의 step 들은 대부분 직렬이지만 다음은 병렬 가능:
- Step 1 (README) 와 Step 2 (compose) 는 독립.
- Step 3 (user JWT fix) 는 Step 4-7 (gateway) 와 별 모듈이라 독립.

자율 모드로 단일 세션에서 진행하므로 직렬로 실행한다 (안전성 우선).

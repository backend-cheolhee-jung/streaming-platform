# gateway

video-platform 의 단일 인증 진입점이자 API 게이트웨이.

## 역할

- 모든 클라이언트 요청을 받아 다운스트림 마이크로서비스(`user`, `payment`, `stream`)로 라우팅한다.
- JWT 검증 및 인가를 **여기 한 곳에서만** 수행한다 — 다운스트림은 게이트웨이가 검증한 `user-id` 헤더만 신뢰한다.
- public 엔드포인트(`/users/signup`, `/users/login`, `/users/refresh`, `/actuator/health`) 는 토큰 없이 통과시키고, 그 외는 401 처리한다.

## 언어/프레임워크

- Language: Kotlin 2.0 / JDK 21
- Framework: Spring Boot 3.2.5, Spring Cloud Gateway 4.1.4 (WebFlux 기반)
- Security: Spring Security (Reactive), Nimbus JOSE+JWT
- 빌드: Gradle (`bootJar` task)

## 워크플로우 요약

1. `SecurityWebFilterChain` 이 요청을 받아 public 매처와 secured 매처를 가른다.
2. secured 요청은 `JwtTokenAuthenticationConverter` → `JwtReactiveAuthenticationManager` → `TokenDecoder` 체인으로 검증.
3. 인증 성공 시 `UserContextGatewayFilter` (Spring Cloud Gateway `GlobalFilter`) 가 `user-id` 헤더를 mutate 된 request 에 추가.
4. `RouteLocator` 가 path 기반으로 다운스트림 URL 로 forwarding (path 변형 없음).

## JWT 컨트랙트

- 알고리즘: HS256 (HmacSHA256)
- 클레임:
  - `user-id` (number) — 다운스트림에 `user-id` 헤더로 전달
  - `username` (string)
  - `role` (string, 콤마 구분)
- 발행자: `user` 모듈 (`cherhy.example.util.JwtManager`)
- secret: `application.yml > jwt.secret`. **user 모듈과 동일 값이어야 한다.**

## 로컬 실행

```sh
./gradlew :gateway:bootJar
java -jar gateway/build/libs/gateway.jar    # localhost 프로파일

# 도커 컴포즈에서:
docker compose up gateway
```

## 테스트

```sh
./gradlew :gateway:test
```

# user

회원 도메인 서비스. 회원가입 / 로그인 / 토큰 발급 / 회원 정보 조회·수정·삭제를 담당한다.

## 역할

- 회원가입(`POST /users/signup`), 로그인(`POST /users/login`), 로그아웃(`DELETE /users/logout`) 처리.
- JWT access token 및 refresh token 발급. 토큰은 gateway 가 검증한다.
- 본인 정보 조회(`GET /users/me`), 수정(`PUT /users`), 삭제 등 사용자 자원 관리.
- gateway 가 주입한 `user-id` 헤더 또는 자체 발급 JWT 를 통해 호출자를 식별.

## 언어/프레임워크

- Language: Kotlin 2.0 / JDK 21
- Framework: Ktor 2.3.11 (Netty engine)
- ORM: Exposed 0.49 (Postgres master/slave 구조, R2DBC 가 아닌 JDBC + 코루틴 wrapping)
- DI: Koin
- Auth: auth0 java-jwt (HS256), bcrypt
- Test: kotest

## 워크플로우 요약

- 진입점: `cherhy.example.Application#module` — routing, jackson, jwt, koin DI, database 순으로 구성.
- 외부 의존성: `postgres-master:5432`, `postgres-slave:5433` (master 쓰기 / slave 읽기 분리).
- API:
  - `POST /users/signup` → password salt + bcrypt 인코딩 → `User` 엔티티 생성 → `Authority` 부여 → 자동 로그인.
  - `POST /users/login` → 비밀번호 검증 → access/refresh JWT 발급 (Authorization 헤더 + Refresh-Token 쿠키).
  - `GET /users/me` — gateway 검증 후 `user-id` 헤더 또는 JWT 로 식별.
- 인증: 본 모듈 자체에는 별도 인증 필터가 없고, gateway 에서 검증된 후 `user-id` 헤더로 호출자가 식별된다는 가정.

## JWT 컨트랙트 (gateway 와 정합)

- HS256, secret 은 `application.conf > jwt.secret` (gateway 와 동일 값).
- 클레임: `user-id` (long), `username` (string), `role` (string, 쉼표 구분).

## 로컬 실행

```sh
# DB 부팅
docker compose up -d postgres-master-container postgres-slave-container

# 앱 부팅
./gradlew :user:run
```

## 테스트

```sh
./gradlew :user:test
```

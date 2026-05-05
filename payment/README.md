# payment

결제 도메인 서비스. CQRS + Event Sourcing + Saga 패턴 기반의 결제 처리.

## 역할

- 결제 생성/조회/수정/삭제 (`/payments/**`).
- Axon Framework 로 결제 Aggregate 모델링, Axon Server 가 이벤트 스토어 역할.
- Saga: 결제 → 영상 구매(stream) 사이의 분산 트랜잭션을 Kafka 로 조율 (예정/구현 중).
- gateway 에서 검증된 `user-id` 헤더를 신뢰하여 호출자를 식별 (`@UserIdFromHeader` 어노테이션).

## 언어/프레임워크

- Language: Kotlin 2.0 / JDK 21
- Framework: Spring Boot 3.2.5 (WebFlux), Axon Framework 4.9.3
- DB: PostgreSQL (R2DBC, master/slave 분리, Flyway 마이그레이션)
- Cache: Redis (Redisson lock 포함)
- Messaging: Kafka, Axon Server
- Encryption: Jasypt (설정값 암호화)
- Test: Kotest, Spring MockK, Testcontainers

## 워크플로우 요약

- 진입점: `com.cherhy.payment.PaymentApplication`. r2dbc/jdbc auto-config 를 끄고 직접 구성.
- 패키지 구조: 헥사고날 아키텍처 — `adapter/in/web`, `adapter/out/persistence`, `application/port/in|out`, `application/service|saga`, `domain`.
- 외부 의존성: `postgres-master`, `postgres-slave`, `redis`, `axon-server`, `kafka`.
- 캐시: `@CacheWithLock` 어노테이션으로 cache stampede 방지 (Redis distributed lock).
- 인증: gateway 가 검증한 `user-id` 헤더 신뢰. 본 모듈은 인증을 다시 수행하지 않는다.

## 로컬 실행

```sh
# 인프라 부팅
docker compose up -d postgres-master-container postgres-slave-container redis-container axon-server kafka

# 앱 부팅
./gradlew :payment:bootRun --args='--spring.profiles.active=dev'
```

## 테스트

```sh
./gradlew :payment:test
```

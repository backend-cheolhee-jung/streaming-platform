# stream

영상 업로드 / 스트리밍 서비스 모듈. YouTube/Netflix 형태의 동영상 콘텐츠 도메인.

## 역할

- 게시물(Post) CRUD, 동영상(Video) 업로드 / 부분 콘텐츠(streaming, range) 응답.
- 영상 구매 처리 (Kafka 기반 결제 사가 수신).
- gateway 가 검증한 `user-id` 헤더를 신뢰하여 작성자/구매자 식별.

## 언어/프레임워크

- Language: Kotlin 2.0 / JDK 21
- Framework: Ktor 2.3.11 (Netty)
- ORM: Ktorm 4.0
- DB: PostgreSQL (HikariCP)
- Cache: Redis (Lettuce)
- Storage: MinIO (S3-호환 동영상 객체 저장)
- Messaging: Kafka (vanilla clients, Spring 비의존)
- Test: kotest

## 워크플로우 요약

- 진입점: `com.cherhy.Application#module` — routing, koin DI, cache, kafka consumer 순.
- 외부 의존성: `postgres-master`, `redis`, `kafka`, `minio` (object storage).
- 핵심 엔드포인트:
  - `GET /streams/posts/{post-id}` — 게시물 조회
  - `POST /streams/posts` — 게시물 작성 (영상 업로드 포함)
  - `GET /streams/posts/{post-id}/videos/{video-id}` — 영상 partial content 응답
- 이벤트:
  - 발행: `checkout.complete.v1`, `checkout.aggregate.v1` 토픽으로 결제 결과 통보 (예정).
  - 소비: `KafkaConsumer` 가 결제 완료 이벤트를 수신해 영상 구매 권한 부여.
- 인증: gateway 가 검증한 `user-id` 헤더 신뢰.

## 로컬 실행

```sh
docker compose up -d postgres-master-container redis-container kafka
./gradlew :stream:run
```

## 테스트

```sh
./gradlew :stream:test
```

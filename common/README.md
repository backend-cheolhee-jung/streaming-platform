# common

여러 모듈에서 공유하는 상수, value object, extension function 을 모은 공통 라이브러리.

## 역할

- 도메인 경로 상수 (`User.USER_DOMAIN`, `Payment.PAYMENT_DOMAIN`, `Stream.STREAM_DOMAIN` ...) 정의 — 게이트웨이 라우팅과 다운스트림 라우팅이 같은 값을 참조하도록 한다.
- 헤더/클레임 키 상수: `USER_ID`, `USERNAME`, `ROLE` 등.
- 공용 value object: `UserId`, `Price`, `Keyword`, `PageRequest`, `BaseDate(Time)` 등.
- 공용 extension: `String`, `Int`, `BigDecimal`, `Iterable`, `ZonedDateTime`, `Map`, `Any`.
- 공용 이벤트 정의: `Payment`, `Video` (Kafka payload 모델).

## 언어/프레임워크

- Language: Kotlin 2.0 / JDK 21
- Library 모듈 — Spring 의존성 없이 다른 모든 모듈에서 사용 가능.
- 빌드: 일반 `jar` task (Spring Boot bootJar 미사용).

## 워크플로우 요약

- 진입점: 없음.
- 사용처: 모든 서비스 모듈이 `implementation(project(":common"))` 로 의존.
- 변경 영향이 크기 때문에 추가/수정 시 관련 모듈 컴파일 회귀를 함께 확인.

## 로컬 실행

해당 사항 없음.

## 테스트

```sh
./gradlew :common:test
```

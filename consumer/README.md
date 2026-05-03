# consumer

Kafka 컨슈머 공통 설정을 제공하는 라이브러리 모듈. Spring Boot 애플리케이션이 직접 실행하지 않는다.

## 역할

- 다른 Spring Boot 모듈(`payment` 등)이 의존하여 Kafka 컨슈머를 일관된 설정으로 사용할 수 있도록 한다.
- `ConsumerFactory`, `ConcurrentKafkaListenerContainerFactory` 빈을 제공.
- 기본 설정: `MANUAL_IMMEDIATE` ack mode, `EARLIEST` offset reset, concurrency 3, max-poll 10.

## 언어/프레임워크

- Language: Kotlin 2.0 / JDK 21
- Framework: Spring Boot (Kafka starter) — auto-configuration 빈만 제공.
- Library 모듈 (`bootJar` 비활성, `jar` 만 활성화).

## 워크플로우 요약

- 진입점: 없음 (라이브러리 모듈).
- 사용처: `payment` 모듈이 `implementation(project(":consumer"))` 으로 의존.
- 그룹 ID/topic 등의 세부 설정은 사용처 application.yml 에서 override 가능 (TODO 주석 참고).

## 로컬 실행

해당 사항 없음 — 의존 모듈을 통해 간접 실행.

## 테스트

```sh
./gradlew :consumer:test
```

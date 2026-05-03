# schedule

배치 / 스케줄러 모듈. 정기 작업과 정산 등의 워크플로우를 담당한다.

## 역할

- 주기적 작업 실행 (정산, 헬스체크, 배치 작업 등).
- ShedLock 으로 멀티 인스턴스 환경에서 작업 중복 방지.
- 외부 모듈에 HTTP 호출하여 데이터 집계 / 검사.

## 언어/프레임워크

- Language: Kotlin 2.0 / JDK 21
- Framework: Ktor 2.3.11 (Netty), 자체 Custom 스케줄러 라이브러리 (`com.github.lolmageap.ktor-server-extension`)
- ORM: Exposed 0.49
- DB: PostgreSQL (HikariCP)
- Lock: ShedLock (Exposed 기반)
- Test: kotest

## 워크플로우 요약

- 진입점: `cherhy.com.Application#module`. 라우팅, 데이터베이스, DI, 스케줄러 plugin 순.
- 외부 의존성: `postgres-master:5432` (ShedLock 테이블 + 도메인 데이터), 다른 마이크로서비스 (HTTP 호출 시).
- 정의된 작업: `HealthCheckScheduler` 등 — `SchedulerConfig` 에서 등록.
- 인증: 외부에서 호출되는 routing 이 거의 없고 (관리/health 위주), 필요 시 gateway 통해 진입.

## 로컬 실행

```sh
docker compose up -d postgres-master-container
./gradlew :schedule:run
```

## 테스트

```sh
./gradlew :schedule:test
```

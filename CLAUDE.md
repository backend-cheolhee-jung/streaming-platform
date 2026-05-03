# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# 전체 빌드
./gradlew build

# 특정 모듈 빌드
./gradlew :user:build
./gradlew :payment:build

# Ktor 모듈 실행 (user, stream, schedule)
./gradlew :user:run
./gradlew :stream:run
./gradlew :schedule:run

# Spring Boot 모듈 실행 (gateway, payment)
./gradlew :gateway:bootRun
./gradlew :payment:bootRun

# 전체 테스트
./gradlew test

# 특정 모듈 테스트
./gradlew :user:test
./gradlew :payment:test

# 단일 테스트 클래스 실행
./gradlew :payment:test --tests "com.cherhy.payment.repository.TestR2dbcRepositoryTest"

# Shadow JAR 빌드 (Ktor 모듈 배포용)
./gradlew :user:shadowJar
./gradlew :stream:shadowJar
```

## 인프라 실행

```bash
# 전체 인프라 기동 (PostgreSQL master/slave, Redis, Kafka, MongoDB, Axon Server)
docker compose up -d

# 특정 서비스만
docker compose up -d postgres-master-container redis-container kafka
```

## 아키텍처 개요

### 멀티 모듈 구조

| 모듈 | 프레임워크 | 역할 |
|---|---|---|
| `gateway` | Spring Boot + Spring Cloud Gateway | JWT 인증, 라우팅, X-User-Id 헤더 주입 |
| `payment` | Spring Boot + WebFlux | 결제, Axon Event Sourcing/Saga |
| `user` | Ktor 3.x | 회원가입/로그인, JWT 발급 |
| `stream` | Ktor 3.x | 동영상 업로드/스트리밍 |
| `schedule` | Ktor 3.x | 스케줄러 (ShedLock 기반) |
| `consumer` | Spring Kafka | Kafka 컨슈머 라이브러리 |
| `producer` | Spring Kafka | Kafka 프로듀서 라이브러리 |
| `common` | 순수 Kotlin | 공유 상수, Value Object, 확장 함수 |

### 인증 흐름

JWT는 **gateway에서만 검증**하고, 검증된 `userId`를 `X-User-Id` 헤더로 하위 서비스에 주입한다. 하위 서비스는 JWT를 직접 검증하지 않고 이 헤더를 신뢰한다.

### ORM 매핑

- `user`, `schedule` → **Jetbrains Exposed** (JDBC, DAO 패턴)
- `stream` → **Ktorm 4.x** (interface entity + sequence DSL)
- `payment` → **Spring Data R2DBC** + `R2dbcEntityTemplate` (커스텀 쿼리 빌더)
- `stream` MongoDB 컬렉션 → **kMongo coroutine**

### 헥사고날 아키텍처 (payment 모듈 기준)

```
adapter/in/web/          ← @WebAdapter (Controller)
adapter/out/persistence/ ← @PersistenceAdapter (Repository 구현체)
adapter/out/             ← Cache, 외부 연동
application/port/in/     ← UseCase 인터페이스 + Command 클래스
application/port/out/    ← Port 인터페이스 (DB, 외부 시스템)
application/service/     ← @UseCase 구현체 (포트 조합)
application/saga/        ← Axon Aggregate, Saga, Event
domain/                  ← 도메인 모델, Value Object, Enum
config/                  ← Spring 설정
annotation/              ← 커스텀 스테레오타입 어노테이션
```

### Ktor 모듈 초기화 순서

`Application.module()`에서 `configure*()` 함수 호출 순서가 중요하다. `user` 기준:

```kotlin
configureRouting()
configureJwt()
configureJackson()
configureDependencyInjection()
configureDatabase()
```

## 코드 컨벤션

### Value Object

모든 원시값 래핑은 `@JvmInline value class`를 사용한다. 생성 팩토리는 반드시 `companion object`의 `@JvmStatic fun of()`로 통일한다.

```kotlin
// ✅ 올바른 패턴
@JvmInline
value class PostId(val value: Long) {
    companion object {
        @JvmStatic
        fun of(value: Long) = PostId(value)
    }
}

// ✅ Any에서 변환하는 확장 함수는 도메인 파일 최하단에 선언
fun Any.toPostId() = PostId.of(this as Long)

// ❌ 직접 생성자 호출 금지 (of() 사용)
val id = PostId(1L)
```

### 도메인 팩토리 메서드

`data class` 도메인 객체는 생성자 대신 `companion object`의 `@JvmStatic fun generate*()`를 사용한다.

```kotlin
data class TestDomain(val id: TestId, val name: TestName, val status: TestStatus) : Serializable {
    companion object {
        @JvmStatic
        fun generateTestDomain(id: TestId, name: TestName, status: TestStatus) =
            TestDomain(id = id, name = name, status = status)
    }
}
```

### 함수 시그니처

파라미터가 2개 이상이면 각 파라미터를 반드시 별도 줄에 작성한다. 1개도 줄바꿈을 적용한다.

```kotlin
// ✅
override suspend fun save(
    email: UserEmail,
    name: Username,
    password: UserPassword,
): User

// ❌
override suspend fun save(email: UserEmail, name: Username, password: UserPassword): User
```

### suspend 함수

레포지토리, UseCase, Service의 모든 메서드는 `suspend`로 선언한다. `runBlocking` 중첩은 금지한다.

### 스테레오타입 어노테이션 (payment 모듈)

Spring `@Component` 대신 아래 커스텀 어노테이션을 반드시 사용한다.

| 어노테이션 | 대상 |
|---|---|
| `@WebAdapter` | Controller |
| `@PersistenceAdapter` | Repository 구현체 |
| `@UseCase` | UseCase 구현체 |
| `@ReadService` | 조회 전용 Service |
| `@WriteService` | 쓰기 Service |
| `@Mapper` | 도메인↔엔티티 변환 |

### 엔드포인트 경로 상수

API 경로는 **`common` 모듈의 `EndPoint.kt`** 에서만 정의한다. 개별 모듈에 경로 문자열을 하드코딩하지 않는다.

```kotlin
// common/util/EndPoint.kt
object User {
    const val SIGN_UP = "/users/signup"
    const val LOGIN = "/users/login"
}
```

### Kafka 상수

토픽명, bootstrap server, 설정값은 **`common` 모듈의 `KafkaConstant.kt`** 에서만 정의한다.

### 의존성 추가

새 라이브러리는 반드시 `buildSrc/src/main/kotlin/DependencyVersions.kt`에 버전을 추가하고, `Dependencies.kt`에 좌표를 선언한 뒤 모듈 `build.gradle.kts`에서 import해서 사용한다. 버전을 `build.gradle.kts`에 직접 쓰지 않는다.

### Ktorm 엔티티 (stream 모듈)

```kotlin
// ✅ interface entity + BaseTable 패턴
object Posts : BaseTable<Post>("post") {
    val id = long("id").primaryKey().bindTo { it.id.value }
    val title = varchar("title").bindTo { it.title.value }
}

interface Post : BaseEntity<Post> {
    companion object : BaseEntityFactory<Post>()
    val id: PostId
    var title: PostTitle
}
```

### Exposed 엔티티 (user, schedule 모듈)

```kotlin
// ✅ object Table + DAO Entity 패턴
object Users : BaseLongIdTable("user", "id") {
    val email = varchar("email", 50)
    val password = varchar("password", 100)
}

class User(id: EntityID<UserId>) : BaseEntity(id = id.unwrap(), table = Users) {
    var email by Users.email
    companion object : BaseEntityClass<User>(Users)
}
```

### R2DBC 커스텀 쿼리 (payment 모듈)

`R2dbcEntityTemplate`을 사용할 때 `PostgresDialect.INSTANCE`를 명시하고, reactive Flow 변환에는 `kotlinx.coroutines.reactive.asFlow()`를 사용한다. `.toIterable().asFlow()` 패턴은 블로킹이므로 금지한다.

### 테스트

- 프레임워크: **Kotest FunSpec** + **TestContainers**
- DB가 필요한 통합 테스트는 `TestContainers`의 실제 PostgreSQL 컨테이너를 사용한다
- 각 테스트 종료 후 `afterTest`/`afterEach`에서 테스트 데이터를 정리해 격리한다
- Spring 모듈: `@SpringBootTest` + `kotest-extensions-spring`
- Ktor 모듈: `testApplication { }` 블록 사용

---

## 지향하는 코드 스타일

### 레이어 간 변환은 반드시 명시적 메서드로

Request → Command, Entity → Domain 변환은 반드시 `toCommand()`, `DomainClass::of` 형태로 명시한다. 레이어 간 직접 참조는 허용하지 않는다.

```kotlin
// ✅ Request → Command 변환
data class LoginRequest(val email: String, val password: String) {
    fun toCommand() = LoginCommand(
        email = UserEmail.of(email),
        password = UserPassword.of(password),
    )
}

// ✅ Entity → Domain 변환 (companion object of())
data class UserDomain(...) {
    companion object {
        @JvmStatic
        fun of(user: User) = UserDomain(
            id = UserId.of(user.id.value),
            name = Username.of(user.name),
            ...
        )
    }
}

// ✅ 서비스 계층에서 변환 체이닝
suspend fun get(email: UserEmail) =
    userRepository.findOne(email)
        ?.let(UserDomain::of)
        ?: throw IllegalStateException("User not found")
```

### Service 계층은 Read/Write로 분리

읽기와 쓰기 책임을 별도 클래스로 분리한다. `ReadXxxService`, `WriteXxxService` 네이밍을 따른다.

```kotlin
// ✅
class ReadUserService(private val userRepository: UserRepository) { ... }
class WriteUserService(private val userRepository: UserRepository) { ... }

// ❌
class UserService(private val userRepository: UserRepository) {
    fun read() { ... }
    fun write() { ... }
}
```

### UseCase는 여러 Service를 조합하는 단일 진입점

UseCase는 트랜잭션 경계를 정의하고 여러 서비스를 조합한다. 단일 서비스 호출만 있다면 UseCase 없이 서비스를 직접 호출한다.

```kotlin
// ✅ 여러 서비스 조합
class SignUpUseCase(
    private val readUserService: ReadUserService,
    private val writeUserService: WriteUserService,
    private val writeAuthorityService: WriteAuthorityService,
) {
    suspend fun execute(command: SignUpCommand) = reactiveTransaction {
        readUserService.checkIfExists(command.email)
        val user = writeUserService.create(...)
        writeAuthorityService.createAuthority(user.id, Role.UNPAID_MEMBER)
    }
}
```

### 트랜잭션은 `reactiveTransaction(READ_ONLY/WRITE)` 로 명시

Ktor 모듈에서 트랜잭션 타입을 명시하고, READ_ONLY 트랜잭션에서 쓰기 연산이 실행되면 런타임에 예외를 발생시키는 인터셉터가 등록되어 있다.

```kotlin
// ✅ 읽기/쓰기 명시
suspend fun execute(command: LoginCommand) = reactiveTransaction(READ_ONLY) { ... }
suspend fun execute(command: SignUpCommand) = reactiveTransaction { ... }  // 기본값 WRITE
```

### Command 클래스는 `init { require(...) }` 로 도메인 불변식 검증

```kotlin
data class SignUpCommand(
    val password: UserPassword,
    val confirmPassword: UserPassword,
) {
    init {
        require(password == confirmPassword) { "password and confirmPassword must be same" }
    }
}
```

### 유틸/상수는 `object` 싱글턴

인스턴스가 필요 없는 유틸리티는 `class`가 아닌 `object`로 선언한다.

```kotlin
// ✅
object Encoder {
    fun encode(value: String) = BCrypt.withDefaults().hashToString(12, value.toCharArray())!!
}

// ❌
class Encoder {
    fun encode(value: String) = ...
}
```

### 확장 프로퍼티로 HTTP 파싱 추상화

라우터에서 헤더/쿠키/경로변수 파싱은 확장 프로퍼티로 추출해 라우터 코드를 의도 중심으로 유지한다.

```kotlin
// ✅ extension 파일에서 선언
val ApplicationCall.jwt
    get() = this.principal<JWTPrincipal>() ?: throw AccessDeniedException("Invalid token")

val Headers.userId
    get() = this[USER_ID]?.toLongOrNull()?.toUserId()
        ?: throw IllegalArgumentException("$USER_ID header is required")

// ✅ 라우터에서 사용
get(GET_ME) {
    val userId = call.jwt.userId
    ...
}
```

### Koin DI는 `by inject<>()` 지연 주입

Ktor 라우터에서 의존성은 `by inject<>()` 위임 프로퍼티로 주입한다. 생성자 주입은 Koin 모듈 정의에서만 사용한다.

```kotlin
// ✅
fun Route.user() {
    val signUpUseCase by inject<SignUpUseCase>()
    val readUserService by inject<ReadUserService>()
    ...
}
```

### 테스트 픽스처는 `EntityFactory` object로 집중 관리

테스트에서 객체 생성은 `EntityFactory`에 모아두고 기본값을 제공해 필요한 필드만 오버라이드한다.

```kotlin
// ✅
object EntityFactory {
    fun generateTestR2dbcEntity(
        id: Long = 0,
        name: String = "test",
        status: String = "ACTIVE",
    ) = TestR2dbcEntity(id = id, name = name, status = status)
}

// 테스트에서
val entity = EntityFactory.generateTestR2dbcEntity(id = 1)
```

### 테스트는 Kotest BehaviorSpec (Given/When/Then)

Spring 통합 테스트는 `BehaviorSpec`, 도메인/단위 테스트는 `FunSpec`을 사용한다.

```kotlin
// ✅ Spring 통합 테스트
@SpringBootTest
class CacheTest(...) : WithTestContainers, BehaviorSpec({
    afterEach { /* 정리 */ }

    Given("캐시가 없는 상태에서") {
        When("조회하면") {
            Then("DB를 호출하고 캐시에 저장한다") { ... }
        }
    }
})
```

### nullable 처리는 `?.let` + `?: throw` 체이닝

`if (x == null) throw` 대신 Kotlin 관용 체이닝을 사용한다.

```kotlin
// ✅
val user = userRepository.findOne(email)
    ?.let(UserDomain::of)
    ?: throw IllegalStateException("User not found")

// ❌
val entity = userRepository.findOne(email)
if (entity == null) throw IllegalStateException("User not found")
val user = UserDomain.of(entity)
```

---

## 지양하는 코드 스타일

### `!!` 강제 언래핑 금지

`awaitSingleOrNull()!!`, `findById(id)!!` 등은 NPE를 숨기는 시한폭탄이다. 항상 `?: defaultValue` 또는 `?: throw`로 처리한다.

```kotlin
// ❌
val count = template.count().awaitSingleOrNull()!!

// ✅
val count = template.count().awaitSingleOrNull() ?: 0L
```

### `runBlocking` 중첩 금지

`suspend` 컨텍스트 안에서 `runBlocking`을 다시 호출하면 스레드가 블로킹된다. Exposed의 `newSuspendedTransaction`을 사용하고, 내부에서는 `suspend` 함수를 직접 호출한다.

```kotlin
// ❌
newSuspendedTransaction {
    runBlocking { repository.findAll() }
}

// ✅
newSuspendedTransaction {
    repository.findAll()
}
```

### Reactive stream을 `.toIterable().asFlow()`로 변환 금지

R2DBC reactive stream을 `.toIterable()`로 변환하면 블로킹 I/O가 발생한다. `kotlinx.coroutines.reactive.asFlow()`를 사용한다.

```kotlin
// ❌
.all().toIterable().asFlow()

// ✅
.all().asFlow()  // kotlinx.coroutines.reactive.asFlow
```

### Value Object 직접 생성자 호출 금지

`of()` 팩토리 메서드를 우회하면 생성 로직을 집중 관리할 수 없다.

```kotlin
// ❌
val id = PostId(1L)

// ✅
val id = PostId.of(1L)
```

### 레이어 경계를 넘는 타입 노출 금지

Controller가 도메인 객체를 직접 반환하거나, Repository가 도메인 객체를 파라미터로 받으면 안 된다.

```kotlin
// ❌ Controller → 도메인 타입 직접 반환
@GetMapping
suspend fun get(): UserDomain { ... }

// ✅ DTO로 변환
@GetMapping
suspend fun get(): UserResponse { ... }
```

### `var` 가변 변수 남용 금지

로컬 변수는 가능하면 `val`로 선언한다. 조건별 분기가 필요한 경우 `buildList { }` 등 함수형 빌더를 활용한다.

```kotlin
// ❌
var criteria: Criteria? = null
if (name != null) criteria = Criteria.where("name").is(name)
if (status != null) criteria = criteria?.and("status")?.is(status) ?: Criteria.where("status").is(status)

// ✅
val criteriaList = buildList {
    name?.let { add(Criteria.where("name").`is`(it)) }
    status?.let { add(Criteria.where("status").`is`(it)) }
}
val criteria = criteriaList.reduce(Criteria::and)
```

### 경로/토픽 문자열 모듈 내 하드코딩 금지

```kotlin
// ❌
@PostMapping("/users/signup")
post("/users/signup")

// ✅ common 모듈의 상수 사용
@PostMapping(User.SIGN_UP)
post(SIGN_UP)
```

### `@Component` 직접 사용 금지 (payment 모듈)

역할이 불명확한 `@Component` 대신 커스텀 스테레오타입 어노테이션을 사용해 아키텍처 역할을 코드로 표현한다.

```kotlin
// ❌
@Component
class RegisterTestService(...) { ... }

// ✅
@UseCase
class RegisterTestService(...) { ... }
```

### 시크릿/설정값 코드 내 하드코딩 금지

DB 비밀번호, JWT 시크릿은 반드시 환경변수(`${?ENV_VAR}`)로 외부화한다. 기본값은 개발 환경용으로만 허용한다.

```
# ❌
secret = "hardcoded-secret"

# ✅
secret = ${?JWT_SECRET}
secret = ${?JWT_SECRET} "dev-default-secret"
```

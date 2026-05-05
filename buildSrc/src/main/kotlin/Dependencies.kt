import DependencyVersions.ASPECTJ_WEAVER_VERSION
import DependencyVersions.JUNIT_PLATFORM_LAUNCHER_VERSION
import DependencyVersions.KAFKA_CLIENTS_VERSION
import DependencyVersions.AXON_VERSION
import DependencyVersions.BCRYPT_VERSION
import DependencyVersions.COROUTINES_VERSION
import DependencyVersions.CRON_UTILS_VERSION
import DependencyVersions.EXPOSED_VERSION
import DependencyVersions.FLYWAY_VERSION
import DependencyVersions.HIKARI_CP_VERSION
import DependencyVersions.JACKSON_DATA_TYPE_JSR310_VERSION
import DependencyVersions.JASYPT_SPRING_BOOT_STARTER_VERSION
import DependencyVersions.JUNIT_VERSION
import DependencyVersions.KMONGO_VERSION
import DependencyVersions.KMSL_VERSION
import DependencyVersions.KOIN_KTOR_VERSION
import DependencyVersions.KOTEST_EXTENSIONS_TEST_CONTAINERS_VERSION
import DependencyVersions.KOTEST_SPRING_EXTENSION_VERSION
import DependencyVersions.KOTLIN_LOGGING_VERSION
import DependencyVersions.JUNIT5_VERSION
import DependencyVersions.KOTLIN_VERSION
import DependencyVersions.TEST_CONTAINERS_VERSION
import DependencyVersions.KOTLINX_SERIALIZATION_JSON_VERSION
import DependencyVersions.KTORM_VERSION
import DependencyVersions.KTOR_EXTENSION_VERSION
import DependencyVersions.LETTUCE_VERSION
import DependencyVersions.LOGBACK_VERSION
import DependencyVersions.MINIO_VERSION
import DependencyVersions.NIMBUS_JWT_VERSION
import DependencyVersions.POSTGRESQL_VERSION
import DependencyVersions.R2DBC_POOL_VERSION
import DependencyVersions.R2DBC_POSTGRESQL_VERSION
import DependencyVersions.REDISSON_VERSION
import DependencyVersions.RESTDOCS_VERSION
import DependencyVersions.SPRING_CLOUD_STARTER_GATEWAY_VERSION
import DependencyVersions.SPRING_KAFKA_VERSION
import DependencyVersions.SPRING_MOCKK_VERSION
import DependencyVersions.SPRING_SECURITY_JWT_VERSION
import PluginVersions.KTOR_PLUGIN_VERSION
import PluginVersions.SPRING_BOOT_VERSION

object Dependencies {
    object Kotlin {
        const val KOTLIN_REFLECT = "org.jetbrains.kotlin:kotlin-reflect"
        const val KOTLIN_STD_LIB = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$KOTLIN_VERSION"
        const val JACKSON_MODULE_KOTLIN = "com.fasterxml.jackson.module:jackson-module-kotlin"
        const val JACKSON_DATA_TYPE_JSR310 = "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$JACKSON_DATA_TYPE_JSR310_VERSION"
        const val REACTOR_KOTLIN_EXTENSIONS = "io.projectreactor.kotlin:reactor-kotlin-extensions"
        const val SERIALIZATION_JSON = "org.jetbrains.kotlinx:kotlinx-serialization-json:$KOTLINX_SERIALIZATION_JSON_VERSION"
    }

    object SpringBootStarters {
        const val SPRING_CLOUD_STARTER_GATEWAY = "org.springframework.cloud:spring-cloud-starter-gateway:$SPRING_CLOUD_STARTER_GATEWAY_VERSION"
        const val SPRING_BOOT_STARTER_ACTUATOR = "org.springframework.boot:spring-boot-starter-actuator:$SPRING_BOOT_VERSION"
        const val SPRING_BOOT_STARTER_WEBFLUX = "org.springframework.boot:spring-boot-starter-webflux:$SPRING_BOOT_VERSION"
        const val SPRING_BOOT_STARTER_VALIDATION = "org.springframework.boot:spring-boot-starter-validation:$SPRING_BOOT_VERSION"
        const val SPRING_BOOT_STARTER_DATA_R2DBC = "org.springframework.boot:spring-boot-starter-data-r2dbc:$SPRING_BOOT_VERSION"
        const val SPRING_BOOT_STARTER_SECURITY = "org.springframework.boot:spring-boot-starter-security:$SPRING_BOOT_VERSION"
        const val SPRING_BOOT_STARTER_DATA_MONGO_DB = "org.springframework.boot:spring-boot-starter-data-mongodb:$SPRING_BOOT_VERSION"
        const val SPRING_BOOT_STARTER_DATA_REDIS_REACTIVE = "org.springframework.boot:spring-boot-starter-data-redis-reactive:$SPRING_BOOT_VERSION"
    }

    object Ktor {
        const val KTOR_SERVER_CORE_JVM = "io.ktor:ktor-server-core-jvm:$KTOR_PLUGIN_VERSION"
        const val KTOR_SERVER_NETTY_JVM = "io.ktor:ktor-server-netty-jvm:$KTOR_PLUGIN_VERSION"
        const val KTOR_KOIN = "io.insert-koin:koin-ktor:$KOIN_KTOR_VERSION"
        const val KTOR_CLIENT_CIO_JVM = "io.ktor:ktor-client-cio-jvm:$KTOR_PLUGIN_VERSION"
        const val KTOR_SERVER_CIO_JVM = "io.ktor:ktor-server-cio-jvm:$KTOR_PLUGIN_VERSION"
        const val KTOR_SERIALIZATION_KOTLINX_JSON_JVM = "io.ktor:ktor-serialization-kotlinx-json-jvm:$KTOR_PLUGIN_VERSION"
        const val KTOR_SERVER_CONFIG_YAML = "io.ktor:ktor-server-config-yaml:$KTOR_PLUGIN_VERSION"
        const val KTOR_SERIALIZATION_JACKSON_JVM = "io.ktor:ktor-serialization-jackson-jvm:$KTOR_PLUGIN_VERSION"
        const val KTOR_SERVER_CALL_LOGGING_JVM = "io.ktor:ktor-server-call-logging-jvm:$KTOR_PLUGIN_VERSION"
        const val KTOR_SERVER_SWAGGER_JVM = "io.ktor:ktor-server-swagger-jvm:$KTOR_PLUGIN_VERSION"
        const val KTOR_SERVER_WEBSOCKETS_JVM = "io.ktor:ktor-server-websockets-jvm:$KTOR_PLUGIN_VERSION"
        const val KTOR_SERVER_STATUS_PAGES = "io.ktor:ktor-server-status-pages:$KTOR_PLUGIN_VERSION"
        const val KTOR_SERVER_PARTIAL_CONTENT = "io.ktor:ktor-server-partial-content:$KTOR_PLUGIN_VERSION"
        const val KTOR_SERVER_CONTENT_NEGOTIATION_JVM = "io.ktor:ktor-server-content-negotiation-jvm:$KTOR_PLUGIN_VERSION"
        const val KTOR_SERVER_AUTH_JVM = "io.ktor:ktor-server-auth-jvm:$KTOR_PLUGIN_VERSION"
        const val KTOR_SERVER_AUTH_JWT_JVM = "io.ktor:ktor-server-auth-jwt-jvm:$KTOR_PLUGIN_VERSION"
    }

    object Database {
        const val HIKARI_CP = "com.zaxxer:HikariCP:$HIKARI_CP_VERSION"
        const val POSTGRESQL = "org.postgresql:postgresql:$POSTGRESQL_VERSION"
    }

    object Exposed {
        const val EXPOSED_CORE = "org.jetbrains.exposed:exposed-core:$EXPOSED_VERSION"
        const val EXPOSED_R2DBC = "org.jetbrains.exposed:exposed-r2dbc:$EXPOSED_VERSION"
        const val EXPOSED_JAVA_TIME = "org.jetbrains.exposed:exposed-java-time:$EXPOSED_VERSION"
        const val EXPOSED_JSON = "org.jetbrains.exposed:exposed-json:$EXPOSED_VERSION"
        const val EXPOSED_DAO = "org.jetbrains.exposed:exposed-dao:$EXPOSED_VERSION"
        const val EXPOSED_JDBC = "org.jetbrains.exposed:exposed-jdbc:$EXPOSED_VERSION"
    }

    object Ktorm {
        const val KTORM_CORE = "org.ktorm:ktorm-core:$KTORM_VERSION"
        const val KTORM_SUPPORT_POSTGRESQL = "org.ktorm:ktorm-support-postgresql:$KTORM_VERSION"
    }

    object KMongo {
        const val KMONGO_COROUTINE_CORE = "org.litote.kmongo:kmongo-coroutine-core:$KMONGO_VERSION"
        const val KMONGO_COROUTINE = "org.litote.kmongo:kmongo-coroutine:$KMONGO_VERSION"
    }

    object Security {
        const val BCRYPT = "at.favre.lib:bcrypt:$BCRYPT_VERSION"
        const val NIMBUS_JWT = "com.nimbusds:nimbus-jose-jwt:$NIMBUS_JWT_VERSION"
        const val SPRING_SECURITY_JWT = "org.springframework.security:spring-security-jwt:$SPRING_SECURITY_JWT_VERSION"
        const val JASYPT_SPRING_BOOT_STARTER = "com.github.ulisesbocchio:jasypt-spring-boot-starter:$JASYPT_SPRING_BOOT_STARTER_VERSION"
    }

    object Axon {
        const val AXON_CONFIGURATION = "org.axonframework:axon-configuration:$AXON_VERSION"
        const val AXON_SPRING_BOOT_STARTER = "org.axonframework:axon-spring-boot-starter:$AXON_VERSION"
    }

    object R2dbc {
        const val R2DBC_POOL = "io.r2dbc:r2dbc-pool:$R2DBC_POOL_VERSION"
        const val R2DBC_POSTGRESQL = "org.postgresql:r2dbc-postgresql:$R2DBC_POSTGRESQL_VERSION"
    }

    object Flyway {
        const val FLYWAY_CORE = "org.flywaydb:flyway-core:$FLYWAY_VERSION"
        const val FLYWAY_DATABASE_POSTGRESQL = "org.flywaydb:flyway-database-postgresql:$FLYWAY_VERSION"
    }

    object Coroutines {
        const val KOTLIN_COROUTINES_CORE = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$COROUTINES_VERSION"
        const val KOTLIN_COROUTINES_JDK8 = "org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$COROUTINES_VERSION"
        const val KOTLIN_COROUTINES_REACTIVE = "org.jetbrains.kotlinx:kotlinx-coroutines-reactive:$COROUTINES_VERSION"
        const val KOTLIN_COROUTINES_REACTOR = "org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$COROUTINES_VERSION"
    }

    object Test {
        const val KOTLIN_TEST_JUNIT = "org.jetbrains.kotlin:kotlin-test-junit:$KOTLIN_VERSION"
        const val KOTEST_RUNNER_JUNIT5 = "io.kotest:kotest-runner-junit5:$JUNIT_VERSION"
        const val KOTEST_ASSERTIONS_CORE = "io.kotest:kotest-assertions-core:$JUNIT_VERSION"
        const val PLATFORM_LAUNCHER = "org.junit.platform:junit-platform-launcher:$JUNIT_PLATFORM_LAUNCHER_VERSION"
        const val JUNIT_VINTAGE_ENGINE = "org.junit.vintage:junit-vintage-engine:$JUNIT5_VERSION"
        const val KOTEST_EXTENSIONS_TEST_CONTAINERS = "io.kotest.extensions:kotest-extensions-testcontainers:$KOTEST_EXTENSIONS_TEST_CONTAINERS_VERSION"
        const val KOTLIN_TEST_JUNIT5 = "org.jetbrains.kotlin:kotlin-test-junit5"
        const val SPRING_BOOT_STARTER_TEST = "org.springframework.boot:spring-boot-starter-test"
        const val KOTEST_EXTENSIONS_SPRING = "io.kotest.extensions:kotest-extensions-spring:$KOTEST_SPRING_EXTENSION_VERSION"
        const val RESTDOCS_WEBTESTCLIENT = "org.springframework.restdocs:spring-restdocs-webtestclient:$RESTDOCS_VERSION"
        const val REACTOR_TEST = "io.projectreactor:reactor-test"
        const val TEST_CONTAINERS_POSTGRESQL = "org.testcontainers:postgresql:$TEST_CONTAINERS_VERSION"
        const val TEST_CONTAINERS_R2DBC = "org.testcontainers:r2dbc:$TEST_CONTAINERS_VERSION"
        const val TEST_CONTAINERS_JUNIT_JUPITER = "org.testcontainers:junit-jupiter:$TEST_CONTAINERS_VERSION"
        const val TEST_CONTAINERS_MONGODB = "org.testcontainers:mongodb:$TEST_CONTAINERS_VERSION"
        const val SPRING_MOCKK = "com.ninja-squad:springmockk:$SPRING_MOCKK_VERSION"
        const val KTOR_SERVER_TESTS_JVM = "io.ktor:ktor-server-tests-jvm"
        const val SPRING_TEST_CONTAINERS = "org.springframework.boot:spring-boot-testcontainers"
        const val SPRING_SECURITY_TEST = "org.springframework.security:spring-security-test"
        const val KTOR_SERVER_TEST_HOST_JVM = "io.ktor:ktor-server-test-host-jvm:$KTOR_PLUGIN_VERSION"
    }

    object Logging {
        const val KOTLIN_LOGGING = "io.github.microutils:kotlin-logging:$KOTLIN_LOGGING_VERSION"
        const val LOGBACK_CLASSIC = "ch.qos.logback:logback-classic:$LOGBACK_VERSION"
    }

    object Kafka {
        const val SPRING_KAFKA = "org.springframework.kafka:spring-kafka:$SPRING_KAFKA_VERSION"
        const val KAFKA_CLIENTS = "org.apache.kafka:kafka-clients:$KAFKA_CLIENTS_VERSION"
    }

    object Custom {
        const val KMSL = "com.github.lolmageap:kmsl:$KMSL_VERSION"
        const val SCHEDULER = "com.github.lolmageap.ktor-server-extension:scheduler:$KTOR_EXTENSION_VERSION"
        const val EXPOSED_SHEDLOCK = "com.github.lolmageap.ktor-server-extension:exposed-shedlock:$KTOR_EXTENSION_VERSION"
    }

    object Redis {
        const val LETTUCE_CORE = "io.lettuce:lettuce-core:$LETTUCE_VERSION"
        const val SPRING_REDISSON = "org.redisson:redisson-spring-boot-starter:$REDISSON_VERSION"
    }

    object Other {
        const val MINIO = "io.minio:minio:$MINIO_VERSION"
        const val DOCKER_COMPOSE = "org.springframework.boot:spring-boot-docker-compose"
        const val CRON_UTILS = "com.cronutils:cron-utils:$CRON_UTILS_VERSION"
        const val ASPECTJ_WEAVER = "org.aspectj:aspectjweaver:$ASPECTJ_WEAVER_VERSION"
        const val MUTINY_KOTLIN = "io.smallrye.reactive:mutiny-kotlin:$KOTLIN_VERSION"
    }
}
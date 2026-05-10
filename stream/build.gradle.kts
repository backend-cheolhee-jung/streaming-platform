import Dependencies.Database
import Dependencies.KMongo
import Dependencies.Kafka
import Dependencies.Kotlin
import Dependencies.Ktor
import Dependencies.Ktorm
import Dependencies.Logging
import Dependencies.Other
import Dependencies.Redis
import Dependencies.Test

plugins {
	kotlin(Plugins.JVM) version PluginVersions.KOTLIN_VERSION
	id(Plugins.KTOR_PLUGIN) version PluginVersions.KTOR_PLUGIN_VERSION
	id(Plugins.SHADOW_JAR) version PluginVersions.SHADOW_JAR_VERSION
	kotlin(Plugins.SERIALIZATION) version PluginVersions.KOTLIN_VERSION
}

application {
	mainClass.set("io.ktor.server.netty.EngineMain")

	val isDevelopment: Boolean = project.ext.has("development")
	applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
	implementation(project(UtilityModules.COMMON))

	implementation(Kotlin.SERIALIZATION_JSON)
	implementation(Ktor.KTOR_SERVER_CORE_JVM)
	implementation(Ktor.KTOR_SERVER_NETTY_JVM)
	implementation(Ktor.KTOR_SERIALIZATION_KOTLINX_JSON_JVM)
	implementation(Ktor.KTOR_SERVER_CONFIG_YAML)
	implementation(Ktor.KTOR_SERIALIZATION_JACKSON_JVM)
	implementation(Ktor.KTOR_SERVER_CALL_LOGGING_JVM)
	implementation(Ktor.KTOR_SERVER_SWAGGER_JVM)
	implementation(Ktor.KTOR_SERVER_WEBSOCKETS_JVM)
	implementation(Ktor.KTOR_SERVER_STATUS_PAGES)
	implementation(Ktor.KTOR_SERVER_PARTIAL_CONTENT)

	implementation(Database.POSTGRESQL)
	implementation(Logging.LOGBACK_CLASSIC)

	implementation(Other.MINIO)
	implementation(Redis.LETTUCE_CORE)

	implementation(Kafka.KAFKA_CLIENTS)
	implementation(Database.HIKARI_CP)

	implementation(Ktor.KTOR_KOIN)
	implementation(Ktorm.KTORM_CORE)
	implementation(Ktorm.KTORM_SUPPORT_POSTGRESQL)

	implementation(KMongo.KMONGO_COROUTINE_CORE)
	implementation(KMongo.KMONGO_COROUTINE)

	testImplementation(Test.KOTEST_RUNNER_JUNIT5)
	testImplementation(Test.KOTEST_ASSERTIONS_CORE)
	testImplementation(Test.TEST_CONTAINERS_POSTGRESQL)
	testImplementation(Test.TEST_CONTAINERS_JUNIT_JUPITER)
	testImplementation(Database.POSTGRESQL)
}

java {
	sourceCompatibility = JavaVersion.VERSION_21
}

tasks.shadowJar {
	enabled = true
	archiveFileName.set("${project.name}.jar")
	mergeServiceFiles()

	manifest {
		attributes(
			"Main-Class" to "io.ktor.server.netty.EngineMain"
		)
	}
}

tasks.jar {
	enabled = false
}
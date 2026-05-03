import Dependencies.Coroutines
import Dependencies.Exposed
import Dependencies.Kotlin
import Dependencies.Ktor
import Dependencies.Logging
import Dependencies.R2dbc
import Dependencies.Security
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

    implementation(Logging.KOTLIN_LOGGING)
    implementation(Logging.LOGBACK_CLASSIC)
    implementation(Kotlin.SERIALIZATION_JSON)
    implementation(Kotlin.JACKSON_DATA_TYPE_JSR310)
    implementation(Ktor.KTOR_SERVER_CORE_JVM)
    implementation(Ktor.KTOR_SERVER_NETTY_JVM)
    implementation(Ktor.KTOR_SERIALIZATION_JACKSON_JVM)
    implementation(Ktor.KTOR_SERIALIZATION_KOTLINX_JSON_JVM)
    implementation(Ktor.KTOR_SERVER_CONFIG_YAML)
    implementation(Ktor.KTOR_SERVER_CALL_LOGGING_JVM)
    implementation(Ktor.KTOR_SERVER_CONTENT_NEGOTIATION_JVM)
    implementation(Ktor.KTOR_SERVER_STATUS_PAGES)
    implementation(Ktor.KTOR_KOIN)

    implementation(Exposed.EXPOSED_R2DBC)
    implementation(Exposed.EXPOSED_JAVA_TIME)
    implementation(R2dbc.R2DBC_POSTGRESQL)
    implementation(R2dbc.R2DBC_POOL)
    implementation(Coroutines.KOTLIN_COROUTINES_CORE)

    implementation(Security.BCRYPT)
    implementation(Ktor.KTOR_SERVER_AUTH_JVM)
    implementation(Ktor.KTOR_SERVER_AUTH_JWT_JVM)

    testImplementation(Test.KOTEST_RUNNER_JUNIT5)
    testImplementation(Test.KOTEST_ASSERTIONS_CORE)
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

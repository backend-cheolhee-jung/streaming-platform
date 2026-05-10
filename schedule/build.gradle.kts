import Dependencies.Custom
import Dependencies.Database
import Dependencies.Exposed
import Dependencies.Ktor
import Dependencies.Logging
import Dependencies.Test

plugins {
    kotlin(Plugins.JVM) version PluginVersions.KOTLIN_VERSION
    id(Plugins.KTOR_PLUGIN) version PluginVersions.KTOR_PLUGIN_VERSION
    id(Plugins.SHADOW_JAR) version PluginVersions.SHADOW_JAR_VERSION
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(project(UtilityModules.COMMON))

    implementation(Ktor.KTOR_SERVER_CORE_JVM)
    implementation(Ktor.KTOR_SERVER_NETTY_JVM)
    implementation(Database.POSTGRESQL)
    implementation(Exposed.EXPOSED_CORE)
    implementation(Exposed.EXPOSED_DAO)
    implementation(Exposed.EXPOSED_JAVA_TIME)
    implementation(Exposed.EXPOSED_JDBC)

    implementation(Logging.LOGBACK_CLASSIC)
    implementation(Ktor.KTOR_KOIN)
    implementation(Database.HIKARI_CP)
    implementation(Ktor.KTOR_CLIENT_CIO_JVM)
    implementation(Ktor.KTOR_SERVER_CIO_JVM)

    implementation(Custom.SCHEDULER)
    implementation(Custom.EXPOSED_SHEDLOCK)

    testImplementation(Test.KTOR_SERVER_TEST_HOST_JVM)
    testImplementation(Test.KOTLIN_TEST_JUNIT)
    testImplementation(Test.KOTEST_RUNNER_JUNIT5)
    testImplementation(Test.KOTEST_ASSERTIONS_CORE)
    testImplementation(Test.TEST_CONTAINERS_POSTGRESQL)
    testImplementation(Test.TEST_CONTAINERS_JUNIT_JUPITER)
    testImplementation(Database.HIKARI_CP)
    testImplementation(Database.POSTGRESQL)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

tasks.shadowJar {
    enabled = true
    archiveFileName.set("${project.name}.jar")

    manifest {
        attributes(
            "Main-Class" to "io.ktor.server.netty.EngineMain"
        )
    }
}

tasks.jar {
    enabled = false
}

tasks.test {
    useJUnitPlatform()
}

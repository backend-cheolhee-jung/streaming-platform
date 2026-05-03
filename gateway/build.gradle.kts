import Dependencies.Coroutines
import Dependencies.Kotlin
import Dependencies.Other
import Dependencies.Security
import Dependencies.SpringBootStarters
import Dependencies.Test

plugins {
    kotlin(Plugins.JVM) version PluginVersions.KOTLIN_VERSION
    id(Plugins.SPRING_BOOT) version PluginVersions.SPRING_BOOT_VERSION
    id(Plugins.DEPENDENCY_MANAGEMENT) version PluginVersions.DEPENDENCY_MANAGEMENT_VERSION
}

dependencies {
    apply(plugin = Plugins.KOTLIN_SPRING)
    apply(plugin = Plugins.DEPENDENCY_MANAGEMENT)

    implementation(project(UtilityModules.COMMON))

    implementation(Kotlin.KOTLIN_REFLECT)

    implementation(SpringBootStarters.SPRING_CLOUD_STARTER_GATEWAY)
    implementation(SpringBootStarters.SPRING_BOOT_STARTER_ACTUATOR)
    implementation(SpringBootStarters.SPRING_BOOT_STARTER_WEBFLUX)
    implementation(SpringBootStarters.SPRING_BOOT_STARTER_SECURITY)
    implementation(Security.SPRING_SECURITY_JWT)

    implementation(Other.MUTINY_KOTLIN)

    implementation(Kotlin.REACTOR_KOTLIN_EXTENSIONS)

    implementation(Test.RESTDOCS_WEBTESTCLIENT)

    implementation(Coroutines.KOTLIN_COROUTINES_CORE)
    implementation(Coroutines.KOTLIN_COROUTINES_JDK8)
    implementation(Coroutines.KOTLIN_COROUTINES_REACTIVE)
    implementation(Coroutines.KOTLIN_COROUTINES_REACTOR)

    implementation(Security.NIMBUS_JWT)

    developmentOnly(Other.DOCKER_COMPOSE)

    testImplementation(Test.KOTEST_RUNNER_JUNIT5)
    testImplementation(Test.KOTEST_EXTENSIONS_SPRING)
    testImplementation(Test.SPRING_BOOT_STARTER_TEST)
    testImplementation(Test.REACTOR_TEST)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

tasks.bootJar {
    enabled = true
    archiveFileName.set("${project.name}.jar")
}

tasks.jar {
    enabled = false
}
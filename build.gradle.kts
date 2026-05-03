import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    kotlin(Plugins.JVM) version PluginVersions.KOTLIN_VERSION
    kotlin(Plugins.ALL_OPEN) version PluginVersions.KOTLIN_VERSION
    kotlin(Plugins.NO_ARG) version PluginVersions.KOTLIN_VERSION
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

allprojects {
    group = "com.cherhy"
    version = "0.0.1-SNAPSHOT"
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

subprojects {
    if (project.name == "frontend") return@subprojects
    apply(plugin = "kotlin")

    dependencies {
        implementation(Dependencies.Logging.KOTLIN_LOGGING)

        testRuntimeOnly(Dependencies.Test.PLATFORM_LAUNCHER)
        testRuntimeOnly(Dependencies.Test.JUNIT_VINTAGE_ENGINE)
        testImplementation(Dependencies.Test.KOTLIN_TEST_JUNIT)
        testImplementation(Dependencies.Test.KOTEST_ASSERTIONS_CORE)
        testImplementation(Dependencies.Test.KOTEST_EXTENSIONS_TEST_CONTAINERS)
    }

    tasks.withType<KotlinJvmCompile>()
        .configureEach {
            compilerOptions
                .languageVersion
                .set(
                    org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0
                )
        }

    tasks.withType<KotlinCompile> {
        kotlin {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_21)
            }
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.jar {
        enabled = true
    }
}
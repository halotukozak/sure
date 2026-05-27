import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.binaryCompatibilityValidator)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kover)
    alias(libs.plugins.dokka)
}

kotlin {
    jvmToolchain(25)
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

java {
    targetCompatibility = JavaVersion.VERSION_21
    sourceCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation(libs.symbolProcessingApi)
    testImplementation(kotlin("test"))
    testImplementation(libs.kctfork.core)
    testImplementation(libs.kctfork.ksp)
    testImplementation(project(":validation"))
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    testLogging {
        events(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates(group.toString(), "validation-ksp", version.toString())
    pom {
        name.set("validation-ksp")
        description.set("KSP processor that generates validate() extensions for @Validatable classes")
        url.set("https://github.com/halotukozak/sure")
        licenses {
            license {
                name.set("MIT")
                url.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("halotukozak")
                name.set("Bartłomiej Kozak")
            }
        }
        scm {
            url.set("https://github.com/halotukozak/sure")
            connection.set("scm:git:https://github.com/halotukozak/sure.git")
            developerConnection.set("scm:git:ssh://git@github.com/halotukozak/sure.git")
        }
    }
}

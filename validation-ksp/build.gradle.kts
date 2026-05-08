import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    alias(libs.plugins.kotlinJvm)
    `maven-publish`
}

kotlin {
    jvmToolchain(25)
}

dependencies {
    implementation(libs.symbolProcessingApi)
    testImplementation(kotlin("test"))
}

java {
    withSourcesJar()
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    testLogging {
        events(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifactId = "validation-ksp"
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
                }
            }
        }
    }
}

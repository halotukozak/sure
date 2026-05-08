plugins {
    id("buildsrc.convention.kotlin-jvm")
    `maven-publish`
}

dependencies {
    implementation(libs.symbolProcessingApi)
    testImplementation(kotlin("test"))
}

java {
    withSourcesJar()
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

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    alias(libs.plugins.kotlinPluginSerialization)
    `maven-publish`
}

kotlin {
    jvmToolchain(25)

    jvm()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    compilerOptions {
        freeCompilerArgs.addAll("-Xcontext-parameters", "-Xexpect-actual-classes")
    }

    targets.all {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    freeCompilerArgs.addAll("-Xcontext-parameters", "-Xexpect-actual-classes")
                }
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinxSerialization)
            implementation(libs.kotlinxCoroutines)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        pom {
            name.set("validation")
            description.set("Type-safe DSL for value validation in Kotlin Multiplatform")
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

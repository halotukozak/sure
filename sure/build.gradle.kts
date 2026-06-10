plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.binaryCompatibilityValidator)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kover)
    alias(libs.plugins.dokka)
}

kotlin {
    jvmToolchain(25)

    jvm {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    compilerOptions {
        freeCompilerArgs.addAll("-Xexpect-actual-classes")
    }

    targets.all {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    freeCompilerArgs.addAll("-Xexpect-actual-classes")
                }
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinxCoroutines)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

ktlint {
    filter {
        exclude { it.file.path.contains("/commonMain/") }
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates(group.toString(), "sure", version.toString())
    pom {
        name.set("sure")
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
            connection.set("scm:git:https://github.com/halotukozak/sure.git")
            developerConnection.set("scm:git:ssh://git@github.com/halotukozak/sure.git")
        }
    }
}

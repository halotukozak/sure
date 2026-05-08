import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinPluginSerialization)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.binaryCompatibilityValidator)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kover)
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

ktlint {
    filter {
        exclude { it.file.path.contains("/commonMain/") }
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    coordinates(group.toString(), "validation", version.toString())
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
            connection.set("scm:git:https://github.com/halotukozak/sure.git")
            developerConnection.set("scm:git:ssh://git@github.com/halotukozak/sure.git")
        }
    }
}

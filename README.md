# sure

Type-safe value validation DSL for Kotlin Multiplatform.

## Modules

- `:validation` — KMP library (jvm, iosX64, iosArm64, iosSimulatorArm64). Core API: `Validator<T>`, `ValidationScope`, `@Validatable`, built-in checks.
- `:validation-ksp` — KSP processor. Generates `T.validate()` extensions and `validatorFor<T>()` registry for every `@Validatable` class.

## Usage

```kotlin
@Validatable
data class LoginRequest(val username: String, val password: String) {
    companion object {
        val validator = Validator<LoginRequest> {
            field(::username) { notBlank(); lengthIn(1..254) }
            field(::password) { notBlank(); lengthIn(8..1024) }
        }
    }
}

LoginRequest("alice", "hunter22!").validate() // ValidationResult.Valid
```

Consumer Gradle setup:

```kotlin
plugins {
    alias(libs.plugins.ksp)
}

dependencies {
    implementation("io.github.halotukozak:validation:0.1.0")
    ksp("io.github.halotukozak:validation-ksp:0.1.0")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}
```

## Build

Requires JDK 25.

- `./gradlew build` — compile + test
- `./gradlew :validation:jvmTest` — run jvm tests
- `./gradlew publishToMavenLocal` — publish to `~/.m2/repository`

## Releasing

Publishing is wired through the [vanniktech-maven-publish](https://vanniktech.github.io/gradle-maven-publish-plugin/) plugin against Sonatype Central Portal.

Required env vars / `~/.gradle/gradle.properties` entries for a real release:

```properties
mavenCentralUsername=<sonatype user token>
mavenCentralPassword=<sonatype token password>
signingInMemoryKey=<armored GPG private key, newlines as \n>
signingInMemoryKeyPassword=<key passphrase>
signingInMemoryKeyId=<short key id, optional>
```

Bump `version` in `gradle.properties` to a non-`-SNAPSHOT` value, then:

```sh
./gradlew publishAndReleaseToMavenCentral
```

`SNAPSHOT` versions skip signing and only publish to `mavenLocal` / Sonatype snapshots.

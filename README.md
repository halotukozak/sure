# sure

[![CI](https://github.com/halotukozak/sure/actions/workflows/ci.yml/badge.svg)](https://github.com/halotukozak/sure/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/halotukozak/sure/branch/main/graph/badge.svg)](https://codecov.io/gh/halotukozak/sure)

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

### CI

GitHub Actions workflows under `.github/workflows/`:

- `ci.yml` — build + test on every push/PR (jvm + iosSimulatorArm64).
- `snapshot.yml` — publishes a snapshot on every push to `main`/`master`.
- `release.yml` — triggered on tags matching `v*`; bump the version in `gradle.properties`, tag, push, done.

Required GitHub secrets for `release.yml` and `snapshot.yml`:

| Secret | Purpose |
|--------|---------|
| `MAVEN_CENTRAL_USERNAME` | Sonatype Central Portal user token |
| `MAVEN_CENTRAL_PASSWORD` | Sonatype Central Portal token password |
| `SIGNING_KEY` | Armored GPG private key (newlines preserved) |
| `SIGNING_PASSWORD` | Passphrase for the GPG key |
| `SIGNING_KEY_ID` | Short key id (optional) |

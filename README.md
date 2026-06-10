# sure

[![CI](https://github.com/halotukozak/sure/actions/workflows/ci.yml/badge.svg)](https://github.com/halotukozak/sure/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/halotukozak/sure/branch/main/graph/badge.svg)](https://codecov.io/gh/halotukozak/sure)
[![docs](https://img.shields.io/badge/docs-github--pages-blue)](https://halotukozak.github.io/sure/)

Type-safe value validation DSL for Kotlin Multiplatform.

## Modules

- `:sure` — KMP library (jvm, iosX64, iosArm64, iosSimulatorArm64). Core API: `Validator<T>`, `ValidationScope`, `@Validatable`,
  built-in checks.
- `:sure-ksp` — KSP processor. Generates `T.validate()` extensions and `validatorFor<T>()` registry for every `@Validatable` class.

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
  implementation("io.github.halotukozak:sure:0.1.0")
  ksp("io.github.halotukozak:sure-ksp:0.1.0")
}
```

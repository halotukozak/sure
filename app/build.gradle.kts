plugins {
    id("buildsrc.convention.kotlin-jvm")
    alias(libs.plugins.ksp)
    application
}

dependencies {
    implementation(project(":validation"))
    ksp(project(":validation-ksp"))
}

application {
    mainClass = "halotukozak.app.AppKt"
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

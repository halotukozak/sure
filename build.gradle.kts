plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinPluginSerialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.mavenPublish) apply false
    alias(libs.plugins.binaryCompatibilityValidator) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.kover)
    alias(libs.plugins.dokka)
}

dependencies {
    kover(project(":sure"))
    kover(project(":sure-ksp"))
    dokka(project(":sure"))
    dokka(project(":sure-ksp"))
}

dokka {
    moduleName.set("sure")
    pluginsConfiguration.html {
        footerMessage.set("&copy; halotukozak")
    }
}

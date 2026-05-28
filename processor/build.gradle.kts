plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    api(project(":api"))
    implementation(libs.kotlinpoet.ksp)
    implementation(libs.symbol.processing.api)
}
plugins {
    alias(libs.plugins.agp.library)
    alias(libs.plugins.ksp)
}

val projectNamespace: String by rootProject.extra

android {
    namespace = "$projectNamespace.xposed"
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    api(project(":api"))
    implementation(project(":common"))
    ksp(project(":processor"))
    compileOnly(libs.xposed.api)
}
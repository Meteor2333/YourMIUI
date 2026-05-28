plugins {
    alias(libs.plugins.agp.library)
}

val projectNamespace: String by rootProject.extra

android {
    namespace = "$projectNamespace.xposed"
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    implementation(project(":common"))
    compileOnly(libs.xposed.api)
}
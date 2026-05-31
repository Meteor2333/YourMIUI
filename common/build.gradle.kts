plugins {
    alias(libs.plugins.agp.library)
}

val projectNamespace: String by rootProject.extra

android {
    namespace = "$projectNamespace.common"
    androidResources.enable = false
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    api(project(":api"))
    api(libs.android.core)
}
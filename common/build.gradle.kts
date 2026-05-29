plugins {
    alias(libs.plugins.agp.library)
    alias(libs.plugins.kotlin.parcelize)
}

val projectNamespace: String by rootProject.extra

android {
    namespace = "$projectNamespace.common"
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    api(project(":api"))
    api(libs.android.core)
}
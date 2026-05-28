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
    api(libs.android.core)
}
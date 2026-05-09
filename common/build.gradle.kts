plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.parcelize")
}

val projectNamespace: String by rootProject.extra

android {
    namespace = "$projectNamespace.common"
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    api("androidx.core:core-ktx:1.18.0")
}
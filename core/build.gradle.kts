plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

val projectNamespace: String by rootProject.extra

android {
    namespace = "$projectNamespace.core"
}

kotlin {
    jvmToolchain(11)
}
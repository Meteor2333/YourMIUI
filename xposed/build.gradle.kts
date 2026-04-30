plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

val projectNamespace: String by rootProject.extra

android {
    namespace = "$projectNamespace.xposed"

    lint {
        disable.add("ExtraTranslation")
        disable.add("MissingTranslation")
    }
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    implementation(project(":common"))
    compileOnly("de.robv.android.xposed:api:82")

    implementation("androidx.core:core-ktx:1.18.0")
}
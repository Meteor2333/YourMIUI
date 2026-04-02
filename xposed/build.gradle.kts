plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

val projectNamespace: String by rootProject.extra

android {
    namespace = "$projectNamespace.xposed"

    defaultConfig {
        buildConfigField("String", "APPLICATION_ID", "\"$projectNamespace\"")
    }

    buildFeatures {
        buildConfig = true
    }
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    implementation(project(":core"))
    compileOnly("de.robv.android.xposed:api:82")
}
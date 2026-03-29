// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.12.3" apply false
    id("com.android.library") version "8.12.3" apply false
    id("org.jetbrains.kotlin.android") version "2.3.20" apply false
}

subprojects {
    plugins.withId("com.android.base") {
        extensions.configure<com.android.build.gradle.BaseExtension> {
            compileSdkVersion(36)

            defaultConfig {
                minSdk = 24
                targetSdk = 36
                versionCode = 1
                versionName = "1.0"
            }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_11
                targetCompatibility = JavaVersion.VERSION_11
            }
        }
    }
}
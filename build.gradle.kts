plugins {
    alias(libs.plugins.agp.application) apply false
    alias(libs.plugins.agp.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
}

val minSdkVersion = 24
val targetSdkVersion = 37

val projectAppId = "cc.meteormc.yourmiui"
val projectVersionCode = 10
val projectVersionName = "1.3.2"

subprojects {
    plugins.withType<com.android.build.gradle.BasePlugin> {
        extensions.configure<com.android.build.api.dsl.CommonExtension> {
            compileSdk {
                version = release(targetSdkVersion)
            }

            defaultConfig.minSdk = minSdkVersion
            compileOptions.sourceCompatibility = JavaVersion.VERSION_11
            compileOptions.targetCompatibility = JavaVersion.VERSION_11
        }
    }

    plugins.withType<com.android.build.gradle.AppPlugin> {
        extensions.configure<com.android.build.api.dsl.ApplicationExtension> {
            defaultConfig {
                applicationId = projectAppId
                minSdk = minSdkVersion
                targetSdk = targetSdkVersion
                versionCode = projectVersionCode
                versionName = projectVersionName
            }
        }
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
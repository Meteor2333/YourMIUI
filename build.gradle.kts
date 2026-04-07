import com.android.build.gradle.BaseExtension

plugins {
    id("com.android.application") version "8.12.3" apply false
    id("com.android.library") version "8.12.3" apply false
    id("org.jetbrains.kotlin.android") version "2.3.20" apply false
}

val sdkMinVersion by extra(24)
val sdkTargetVersion by extra(36)

val appVersionCode by extra(2)
val appVersionName by extra("1.0.1")

val projectNamespace by extra("cc.meteormc.yourmiui")

subprojects {
    plugins.withId("com.android.base") {
        extensions.configure<BaseExtension> {
            compileSdkVersion(sdkTargetVersion)

            defaultConfig {
                minSdk = sdkMinVersion
                targetSdk = sdkTargetVersion
                versionCode = appVersionCode
                versionName = appVersionName
            }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_11
                targetCompatibility = JavaVersion.VERSION_11
            }
        }
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
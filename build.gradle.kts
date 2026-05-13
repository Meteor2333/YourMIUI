plugins {
    id("com.android.application") version "9.2.1" apply false
    id("com.android.library") version "9.2.1" apply false
    id("org.jetbrains.kotlin.plugin.parcelize") version "2.3.21" apply false
}

val minSdkVersion by extra(26)
val targetSdkVersion by extra(36)
val appVersionCode by extra(9)
val appVersionName by extra("1.3.1")
val projectNamespace by extra("cc.meteormc.yourmiui")

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
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
plugins {
    alias(libs.plugins.agp.application) apply false
    alias(libs.plugins.agp.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
}

val minSdkVersion by extra(24)
val targetSdkVersion by extra(36)
val appVersionCode by extra(10)
val appVersionName by extra("1.3.2")

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
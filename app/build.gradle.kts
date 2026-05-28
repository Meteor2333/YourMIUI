import java.util.Properties

plugins {
    alias(libs.plugins.agp.application)
}

val minSdkVersion: Int by rootProject.extra
val targetSdkVersion: Int by rootProject.extra
val appVersionCode: Int by rootProject.extra
val appVersionName: String by rootProject.extra
val projectNamespace: String by rootProject.extra

android {
    namespace = projectNamespace

    defaultConfig {
        applicationId = projectNamespace
        minSdk = minSdkVersion
        targetSdk = targetSdkVersion
        versionCode = appVersionCode
        versionName = appVersionName
    }

    val keystoreProp = Properties()
    val keystorePropFile = rootProject.file("keystore.properties")
    if (keystorePropFile.exists()) {
        keystoreProp.load(keystorePropFile.inputStream())
    }

    signingConfigs {
        create("release") {
            storeFile = keystoreProp.getProperty("storeFile")?.let { rootProject.file(it) }
            storePassword = keystoreProp.getProperty("storePassword")
            keyAlias = keystoreProp.getProperty("keyAlias")
            keyPassword = keystoreProp.getProperty("keyPassword")
        }
    }

    buildTypes {
        release {
            if (keystorePropFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }

            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    implementation(project(":common"))
    runtimeOnly(project(":xposed"))

    implementation(libs.android.appcompat)
    implementation(libs.android.material)
    implementation(libs.android.recyclerview)
    implementation(libs.android.navigation.fragment)
    implementation(libs.android.navigation.ui)
}
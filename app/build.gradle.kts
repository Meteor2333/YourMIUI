import java.util.*

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val projectNamespace: String by rootProject.extra

android {
    namespace = projectNamespace

    defaultConfig {
        applicationId = projectNamespace
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

    lint {
        disable.add("ExtraTranslation")
        disable.add("MissingTranslation")
    }
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    compileOnly(project(":common"))
    runtimeOnly(project(":xposed"))

    implementation("androidx.core:core-ktx:1.18.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.9.8")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.8")
}
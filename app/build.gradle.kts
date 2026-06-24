import java.util.Properties

plugins {
    alias(libs.plugins.agp.application)
}

android {
    namespace = "cc.meteormc.yourmiui"

    val keystoreProp = Properties()
    val keystorePropFile = rootProject.file("keystore.properties")
    if (keystorePropFile.exists()) {
        keystoreProp.load(keystorePropFile.inputStream())
    }

    signingConfigs {
        val storeFile = providers.gradleProperty("android.storeFile").orNull ?: return@signingConfigs
        val storePassword = providers.gradleProperty("android.storePassword").orNull ?: return@signingConfigs
        val keyAlias = providers.gradleProperty("android.keyAlias").orNull ?: return@signingConfigs
        val keyPassword = providers.gradleProperty("android.keyPassword").orNull ?: return@signingConfigs

        create("release") {
            this.storeFile = rootProject.file(storeFile)
            this.storePassword = storePassword
            this.keyAlias = keyAlias
            this.keyPassword = keyPassword
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("release")
            optimization {
                enable = true
            }
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
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "cc.meteormc.yourmiui.xposed"

    defaultConfig {
        buildConfigField("String", "PACKAGE_NAME", "\"cc.meteormc.yourmiui\"")
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
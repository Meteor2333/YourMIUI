plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "cc.meteormc.yourmiui.xposed"
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    implementation(project(":core"))
    compileOnly("de.robv.android.xposed:api:82")
}
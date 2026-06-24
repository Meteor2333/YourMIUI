plugins {
    alias(libs.plugins.agp.library)
}

android {
    namespace = "cc.meteormc.yourmiui.common"
    androidResources.enable = false
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    api(project(":api"))
    api(libs.android.core)
}
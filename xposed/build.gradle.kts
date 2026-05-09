plugins {
    id("com.android.library")
}

val projectNamespace: String by rootProject.extra

android {
    namespace = "$projectNamespace.xposed"
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    implementation(project(":common"))
    compileOnly("de.robv.android.xposed:api:82")
}
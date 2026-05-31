plugins {
    alias(libs.plugins.agp.library)
    alias(libs.plugins.ksp)
}

val projectNamespace: String by rootProject.extra

android {
    namespace = "$projectNamespace.xposed"

    sourceSets {
        getByName("main") {
            var path: String? = null
            buildTypes {
                debug {
                    path = "generated/ksp-res/debug"
                }

                release {
                    path = "generated/ksp-res/release"
                }
            }

            if (path != null) {
                res.directories.add(layout.buildDirectory.dir(path).get().asFile.path)
            }
        }
    }
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    implementation(project(":common"))
    ksp(project(":processor"))
    compileOnly(libs.xposed.api)
}

val copyKspDebugRes by tasks.registering(Copy::class) {
    dependsOn("kspDebugKotlin")
    from(layout.buildDirectory.dir("generated/ksp/debug/resources/res"))
    into(layout.buildDirectory.dir("generated/ksp-res/debug"))
}

val copyKspReleaseRes by tasks.registering(Copy::class) {
    dependsOn("kspReleaseKotlin")
    from(layout.buildDirectory.dir("generated/ksp/release/resources/res"))
    into(layout.buildDirectory.dir("generated/ksp-res/release"))
}

tasks.named("preBuild") {
    dependsOn(copyKspDebugRes)
    dependsOn(copyKspReleaseRes)
}
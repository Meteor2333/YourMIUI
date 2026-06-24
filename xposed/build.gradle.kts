plugins {
    alias(libs.plugins.agp.library)
    alias(libs.plugins.ksp)
}

android {
    namespace = "cc.meteormc.yourmiui.xposed"

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

tasks.register<Copy>("copyKspDebugRes") {
    dependsOn("kspDebugKotlin")
    from(layout.buildDirectory.dir("generated/ksp/debug/resources/res"))
    into(layout.buildDirectory.dir("generated/ksp-res/debug"))
}

tasks.register<Copy>("copyKspReleaseRes") {
    dependsOn("kspReleaseKotlin")
    from(layout.buildDirectory.dir("generated/ksp/release/resources/res"))
    into(layout.buildDirectory.dir("generated/ksp-res/release"))
}

tasks.named("preBuild") {
    dependsOn("copyKspDebugRes")
    dependsOn("copyKspReleaseRes")
}

plugins {
    kotlin("multiplatform") version "1.4.0"
}

kotlin {
    targets {
        jvm {
            compilations.all {
                kotlinOptions.jvmTarget = "1.8"
            }
        }

        linuxX64()
        mingwX64()
        macosX64()
    }

    targets.all {
        compilations.all {
            kotlinOptions {
                freeCompilerArgs = freeCompilerArgs + arrayOf(
                    "-Xopt-in=kotlin.RequiresOptIn"
                )
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jvmMain by getting
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.3.9")
                implementation("io.mockk:mockk:1.10.2")
            }
        }

        val desktopMain by creating {
            dependsOn(commonMain)
        }
        val macosX64Main by getting {
            dependsOn(desktopMain)
        }

        val mingwX64Main by getting {
            dependsOn(desktopMain)
        }

        val linuxX64Main by getting {
            dependsOn(desktopMain)
        }
    }
}

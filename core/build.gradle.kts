
plugins {
    kotlin("multiplatform") version "1.4.0"
    id("maven")
    id("maven-publish")
}

kotlin {
    targets {
        js {
            browser()
            nodejs()
        }
        jvm {
            compilations.all {
                kotlinOptions.jvmTarget = "1.8"
            }
        }

        ios()
        tvos()
        watchos()
        macosX64()

        linuxX64()
        mingwX64()
    }

    configure(listOf(targets["metadata"], jvm(), js())) {
        mavenPublication {
            val targetPublication = this@mavenPublication
            tasks.withType<AbstractPublishToMaven>()
                .matching { it.publication == targetPublication }
                .all { onlyIf { findProperty("isMainHost") == "true" } }
        }
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

publishing {
    repositories {
        mavenLocal()
        maven {
            val user = "faogustavo"
            val repo = "maven"
            val name = "fluks"

            setUrl("https://api.bintray.com/maven/$user/$repo/$name/;publish=0;override=1")

            credentials {
                username = if(project.hasProperty("bintrayUser")) {
                    project.property("bintrayUser").toString()
                } else {
                    System.getenv("BINTRAY_USER")
                }

                password = if(project.hasProperty("bintrayApiKey")) {
                    project.property("bintrayApiKey").toString()
                } else {
                    System.getenv("BINTRAY_KEY")
                }
            }
        }
    }
}

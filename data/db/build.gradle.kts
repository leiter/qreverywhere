plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.room)
}

kotlin {
    // Opt-in to experimental time API for kotlinx-datetime 0.7.x
    sourceSets.all {
        languageSettings.optIn("kotlin.time.ExperimentalTime")
    }

    // Android target
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }

    // iOS targets (no framework binary - only :shared produces the framework)
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    )

    // Desktop (JVM) target
    jvm("desktop") {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Core module (models, interfaces)
                api(project(":core:base"))

                // Room KMP
                api(libs.androidx.room.runtime)
                api(libs.androidx.sqlite.bundled)

                // Coroutines
                implementation(libs.kotlinx.coroutines.core)
            }
        }

        val androidMain by getting {
            dependencies {
                // Room Android-specific
                implementation(libs.androidx.room.ktx)

                // Android core for createBitmap
                implementation(libs.androidx.core.ktx)

                // Legacy repository bridge removed - migrated to shared Room database
            }
        }

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }

        val desktopMain by getting
    }
}

android {
    namespace = "cut.the.crap.qreverywhere.data.db"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// Room KMP schema export
room {
    schemaDirectory("$projectDir/schemas")
}

// KSP configuration for Room
dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspIosX64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspDesktop", libs.androidx.room.compiler)
}

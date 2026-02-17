plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.compose)
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

    // iOS targets
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    // Desktop (JVM) target
    jvm("desktop") {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }

    // Web (Wasm) target - disabled for now due to dependency compatibility issues
    // Libraries like Napier, ViewModel, and Navigation don't yet support wasmJs
    // @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    // wasmJs {
    //     browser {
    //         commonWebpackConfig {
    //             outputFileName = "qreverywhere.js"
    //         }
    //     }
    //     binaries.executable()
    // }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Coroutines
                implementation(libs.kotlinx.coroutines.core)

                // DateTime - use api so it's available to dependent modules
                api(libs.kotlinx.datetime)

                // Koin DI
                implementation(libs.koin.core)
                implementation(libs.koin.compose)

                // Logging
                implementation(libs.napier)

                // Compose Multiplatform
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)

                // Lifecycle ViewModel (KMP compatible)
                implementation(libs.lifecycle.viewmodel.kmp)

                // Navigation Compose (KMP)
                implementation(libs.navigation.compose.kmp)

                // Room KMP
                implementation(libs.androidx.room.runtime)
                implementation(libs.androidx.sqlite.bundled)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
            }
        }

        val androidMain by getting {
            dependencies {
                // Android-specific dependencies
                implementation(libs.androidx.core.ktx)
                implementation(libs.koin.android)

                // Lifecycle (Android-specific)
                implementation(libs.androidx.lifecycle.runtime.compose)

                // CameraX dependencies
                implementation(libs.androidx.camera.camera2)
                implementation(libs.androidx.camera.lifecycle)
                implementation(libs.androidx.camera.view)
                implementation(libs.androidx.camera.core)

                // Room (Android-specific for now)
                implementation(libs.androidx.room.runtime)
                implementation(libs.androidx.room.ktx)

                // ZXing for QR code operations
                implementation(libs.google.zxing.core)

                // Firebase Crashlytics - use direct version since BOM doesn't work in KMP
                implementation("com.google.firebase:firebase-crashlytics-ktx:19.3.0")

                // Use existing repository
                implementation(project(":qr_repository"))
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

            dependencies {
                // iOS-specific dependencies
            }
        }

        val desktopMain by getting {
            dependencies {
                // Desktop-specific dependencies
                // ZXing for QR code detection
                implementation(libs.google.zxing.core)
            }
        }
    }
}

android {
    namespace = "cut.the.crap.qreverywhere.shared"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
}

compose.resources {
    publicResClass = true
    generateResClass = always
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
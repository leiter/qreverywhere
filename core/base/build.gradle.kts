plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.compose)
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
                // Coroutines
                implementation(libs.kotlinx.coroutines.core)

                // DateTime - use api so it's available to dependent modules
                api(libs.kotlinx.datetime)

                // Koin DI
                implementation(libs.koin.core)

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

                // Activity Compose (for rememberLauncherForActivityResult)
                implementation(libs.androidx.activity.compose)

                // CameraX dependencies
                implementation(libs.androidx.camera.camera2)
                implementation(libs.androidx.camera.lifecycle)
                implementation(libs.androidx.camera.view)
                implementation(libs.androidx.camera.core)

                // ZXing for QR code operations
                implementation(libs.google.zxing.core)

                // Firebase Crashlytics
                implementation("com.google.firebase:firebase-crashlytics-ktx:19.3.0")
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
                // ZXing for QR code detection
                implementation(libs.google.zxing.core)
            }
        }
    }
}

android {
    namespace = "cut.the.crap.qreverywhere.core.base"
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
    packageOfResClass = "qreverywhere.shared.generated.resources"
}

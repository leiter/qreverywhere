plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
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

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Coroutines
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

                // DateTime
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")

                // Koin DI - Using BOM for version management
                implementation(platform("io.insert-koin:koin-bom:4.0.1"))
                implementation("io.insert-koin:koin-core")
                implementation("io.insert-koin:koin-compose")

                // Logging
                implementation("io.github.aakira:napier:2.6.1")

                // Compose Multiplatform
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)

                // Lifecycle ViewModel (KMP compatible)
                implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.0")

                // Navigation Compose
                implementation("org.jetbrains.androidx.navigation:navigation-compose:2.8.0-alpha10")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
            }
        }

        val androidMain by getting {
            dependencies {
                // Android-specific dependencies
                implementation("androidx.core:core-ktx:1.13.1")
                implementation(platform("io.insert-koin:koin-bom:4.0.1"))
                implementation("io.insert-koin:koin-android")

                // Lifecycle (Android-specific)
                implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.0")

                // CameraX dependencies
                implementation("androidx.camera:camera-camera2:1.3.4")
                implementation("androidx.camera:camera-lifecycle:1.3.4")
                implementation("androidx.camera:camera-view:1.3.4")
                implementation("androidx.camera:camera-core:1.3.4")

                // Room (Android-specific for now)
                implementation("androidx.room:room-runtime:2.7.1")
                implementation("androidx.room:room-ktx:2.7.1")

                // ZXing for QR code operations
                implementation("com.google.zxing:core:3.4.0")

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
            }
        }
    }
}

android {
    namespace = "cut.the.crap.qreverywhere.shared"
    compileSdk = 35

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
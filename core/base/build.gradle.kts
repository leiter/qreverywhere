import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.compose)
}

compose.resources {
    packageOfResClass = "cut.the.crap.qreverywhere.core.base.generated.resources"
    publicResClass = true
}

kotlin {
    jvmToolchain(21)

    sourceSets.all {
        languageSettings.optIn("kotlin.time.ExperimentalTime")
    }

    // Android target
    androidTarget()

    // iOS targets
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "base"
            isStatic = true
        }
    }

    // Desktop (JVM) target
    jvm("desktop")

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Compose Multiplatform
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                api(compose.material)
                api(compose.materialIconsExtended)
                implementation(compose.ui)
                implementation(compose.components.resources)

                // Coroutines
                implementation(libs.kotlinx.coroutines.core)

                // Koin
                implementation(libs.koin.core)

                // Logging
                implementation(libs.napier)

                // DateTime
                api(libs.kotlinx.datetime)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val androidMain by getting {
            dependencies {
                // Android-specific dependencies
                implementation(libs.androidx.core.ktx)
                implementation(libs.androidx.camera.core)
                implementation(libs.bundles.camerax)
                implementation(libs.google.zxing.core)
                implementation(libs.guava)
                implementation(libs.androidx.activity.compose)
                implementation(project.dependencies.platform(libs.firebase.bom))
                implementation(libs.firebase.crashlytics)
            }
        }

        val desktopMain by getting {
            dependencies {
                // Desktop-specific dependencies
                implementation(libs.google.zxing.core)
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
    }
}

// Configure JVM compilation tasks with the new DSL
tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
    }
}

android {
    namespace = "cut.the.crap.qreverywhere.core.base"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}
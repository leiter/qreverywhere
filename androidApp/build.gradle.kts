import java.io.FileInputStream
import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    //alias(libs.plugins.google.services)
    //alias(libs.plugins.firebase.crashlytics)
}

// Migrate kotlin compiler options to the new DSL
kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
    }
}

android {
    compileSdk = 36
    namespace = "cut.the.crap.qreverywhere"
    defaultConfig {
        applicationId = "cut.the.crap.qreverywhere"
        minSdk = 23
        targetSdk = 36
        versionName = "1.0"
        versionCode = 12
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] =
                    "$projectDir/schemas"
            }
        }
    }

    val signingPropsFile = File("/home/mandroid/Videos/AA_FILES/qreverywhere_signature_prop")
    val signingProps = Properties()
    if (signingPropsFile.exists()) {
        signingProps.load(FileInputStream(signingPropsFile))
    }

    signingConfigs {
        create("release") {
            storeFile = signingProps["storeFile"]?.let { signingPropsFile.parentFile.resolve(it as String) }
            storePassword = signingProps["storePassword"] as String?
            keyAlias = signingProps["keyAlias"] as String?
            keyPassword = signingProps["keyPassword"] as String?
        }
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = true // Enable R8 for code shrinking/obfuscation
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true // Enable R8 for code shrinking/obfuscation
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    // REMOVE THIS BLOCK - it's deprecated
    // kotlinOptions {
    //     jvmTarget = "21"
    // }

    buildFeatures {
        compose = true
    }
}

dependencies {

    // KMP Shared Module
    implementation(project(":shared"))

    // Compose Multiplatform Resources (for accessing shared string resources)
    implementation(libs.components.resources)

    // Android basics
    implementation(libs.androidx.core.ktx)
    //implementation(libs.androidx.appcompat)
    implementation(libs.google.material)
    //implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.security.crypto)

    // Navigation (Compose only, Fragment navigation removed)
    implementation(libs.androidx.navigation.compose)

    // Jetpack Compose
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(libs.bundles.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Koin for Dependency Injection
    val koinBom = platform(libs.koin.bom)
    implementation(koinBom)
    implementation(libs.bundles.koin)

    // Firebase
    //implementation(platform(libs.firebase.bom))
    //implementation(libs.firebase.crashlytics)
    //implementation(libs.firebase.analytics)

    // Camera
    implementation(libs.bundles.camerax)

    // Qr Related
    implementation(libs.google.zxing.core)

    // Coil for Compose (Kotlin-first image loading)
    implementation(libs.coil.compose)

    // Timber
    implementation(libs.timber)

    // kotlinx-datetime (for KMP compatibility with shared module)
    implementation(libs.kotlinx.datetime)

    // Test (using kotlin-test for KMP compatibility)
    testImplementation(libs.kotlin.test)

    // Instrumentation test
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}
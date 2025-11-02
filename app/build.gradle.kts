import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // navigation.safeargs removed - only needed for Fragment navigation
//    id ("com.localazy.gradle")
}

//localazy {
//    readKey "a7844856686728087529-01fb65759e01803f9c76e6d9eda43baa77a6d93523bb29f2a887506b97385e7a"
//    writeKey "a7844856686728087529-326192c4249d0bbd857fbb6dcaa014c1484f389cbc9b81e1a4f762b0492883d3"
//}

android {
    compileSdk = 35
    namespace = "cut.the.crap.qreverywhere"
    defaultConfig {
        applicationId = "cut.the.crap.qreverywhere"
        minSdk = 21
        targetSdk = 35
        versionName = "1.0"
        versionCode = 10
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] =
                        "$projectDir/schemas"
            }
        }
    }

    buildTypes {
        getByName("debug") {
//            minifyEnabled true
//            shrinkResources true
            //proguardFiles getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
        }
        getByName("release") {
            //minifyEnabled = true
           // proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
//    namespace 'cut.the.crap.qreverywhere'
}

dependencies {

    // KMP Shared Module
    implementation(project(":shared"))

    // Android basics
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.google.material)
    implementation(libs.androidx.constraintlayout)
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

    // Local modules
    implementation(project(":qr_repository"))

    // Camera
    implementation(libs.bundles.camerax)

    // Qr Related
    implementation(libs.google.zxing.core)

    // Coil for Compose (Kotlin-first image loading)
    implementation(libs.coil.compose)

    // Timber
    implementation(libs.timber)

    // kotlinx-datetime (for KMP compatibility with shared module)
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")

    // Test (using kotlin-test for KMP compatibility)
    testImplementation(libs.kotlin.test)

    // Instrumentation test
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}

// Allow usage of Kotlin's @OptIn.
//tasks.withType(KotlinCompile).configureEach {
//    kotlinOptions {
//        freeCompilerArgs += ["-opt-in=kotlin.RequiresOptIn"]
//    }
//}
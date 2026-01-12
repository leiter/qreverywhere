
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    compileSdk = 36
    namespace = "cut.the.crap.qreverywhere"
    defaultConfig {
        applicationId = "cut.the.crap.qreverywhere"
        minSdk = 21
        targetSdk = 36
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
}

dependencies {

    // KMP Shared Module
    implementation(project(":shared"))

    // Compose Multiplatform Resources (for accessing shared string resources)
    implementation(libs.components.resources)

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
    implementation(libs.kotlinx.datetime)

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
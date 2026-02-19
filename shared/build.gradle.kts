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

// Localization validation tasks
tasks.register("checkHardcodedStrings") {
    group = "verification"
    description = "Check for hardcoded strings in Kotlin source files that should use stringResource()"

    doLast {
        val sourceDir = file("src/commonMain/kotlin")
        val violations = mutableListOf<String>()

        // Patterns to detect hardcoded strings
        val patterns = listOf(
            """Text\s*\(\s*"[^"]+"\s*\)""".toRegex(),
            """title\s*=\s*"[^"]+"""".toRegex(),
            """label\s*=\s*"[^"]+"""".toRegex(),
            """contentDescription\s*=\s*"[^"]+"""".toRegex(),
            """description\s*=\s*"[^"]+"""".toRegex(),
            """placeholder\s*=\s*"[^"]+"""".toRegex()
        )

        // Patterns to exclude (valid uses)
        val excludePatterns = listOf(
            "stringResource",
            "Res.string",
            "\${",
            "\"\"\"",  // Multi-line strings
            "// ",     // Comments
            "/*"       // Block comments
        )

        sourceDir.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .forEach { file ->
                file.readLines().forEachIndexed { lineNum, line ->
                    // Skip if line contains exclude patterns
                    if (excludePatterns.any { line.contains(it) }) return@forEachIndexed

                    patterns.forEach { pattern ->
                        if (pattern.containsMatchIn(line)) {
                            // Additional check: skip emoji-only strings like "📷"
                            val match = pattern.find(line)?.value ?: ""
                            if (!match.matches(""".*"[\p{So}\p{Sc}]+".*""".toRegex())) {
                                violations.add("${file.relativeTo(sourceDir)}:${lineNum + 1}: $line")
                            }
                        }
                    }
                }
            }

        if (violations.isNotEmpty()) {
            println("\n⚠️  Found ${violations.size} potential hardcoded strings:")
            violations.forEach { println("  $it") }
            println("\nConsider using stringResource(Res.string.xxx) for localization.\n")
            // Note: Change to throw GradleException(...) to fail the build
        } else {
            println("✅ No hardcoded strings detected.")
        }
    }
}

tasks.register("validateStringResources") {
    group = "verification"
    description = "Validate that all language files have matching string keys"

    doLast {
        val resourcesDir = file("src/commonMain/composeResources")
        val defaultStringsFile = file("$resourcesDir/values/strings.xml")

        if (!defaultStringsFile.exists()) {
            throw GradleException("Default strings.xml not found at ${defaultStringsFile.path}")
        }

        // Parse default string keys
        val defaultKeys = mutableSetOf<String>()
        val keyPattern = """<string\s+name="([^"]+)"[^>]*>""".toRegex()

        defaultStringsFile.readLines().forEach { line ->
            keyPattern.find(line)?.let { match ->
                defaultKeys.add(match.groupValues[1])
            }
        }

        println("Found ${defaultKeys.size} string keys in default strings.xml")

        // Check each language directory
        val errors = mutableListOf<String>()
        resourcesDir.listFiles()
            ?.filter { it.isDirectory && it.name.startsWith("values-") }
            ?.forEach { langDir ->
                val stringsFile = file("${langDir.path}/strings.xml")
                if (stringsFile.exists()) {
                    val langKeys = mutableSetOf<String>()
                    stringsFile.readLines().forEach { line ->
                        keyPattern.find(line)?.let { match ->
                            langKeys.add(match.groupValues[1])
                        }
                    }

                    val missingKeys = defaultKeys - langKeys
                    val extraKeys = langKeys - defaultKeys

                    if (missingKeys.isNotEmpty()) {
                        errors.add("${langDir.name}: Missing ${missingKeys.size} keys: ${missingKeys.take(5).joinToString()}${if (missingKeys.size > 5) "..." else ""}")
                    }
                    if (extraKeys.isNotEmpty()) {
                        errors.add("${langDir.name}: Extra ${extraKeys.size} keys: ${extraKeys.take(5).joinToString()}${if (extraKeys.size > 5) "..." else ""}")
                    }

                    if (missingKeys.isEmpty() && extraKeys.isEmpty()) {
                        println("✅ ${langDir.name}: All ${langKeys.size} keys match")
                    }
                }
            }

        if (errors.isNotEmpty()) {
            println("\n⚠️  String resource validation issues:")
            errors.forEach { println("  $it") }
            // Note: Change to throw GradleException(...) to fail the build
        } else {
            println("\n✅ All language files have matching keys.")
        }
    }
}
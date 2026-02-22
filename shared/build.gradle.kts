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
                // Core module (models, interfaces, theme, camera, utils, resources)
                api(project(":core:base"))

                // Data module (Room DB, DAO, repository)
                api(project(":data:db"))

                // Feature modules
                api(project(":feature:history"))
                api(project(":feature:scan"))
                api(project(":feature:create"))
                api(project(":feature:detail"))
                api(project(":feature:settings"))

                // Coroutines
                implementation(libs.kotlinx.coroutines.core)

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
            }
        }
    }
}

compose.resources {
    publicResClass = true
    generateResClass = never
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

// Localization validation tasks
tasks.register("checkHardcodedStrings") {
    group = "verification"
    description = "Check for hardcoded strings in Kotlin source files that should use stringResource()"

    doLast {
        val sourceDirs = listOf(
            file("src/commonMain/kotlin"),
            rootProject.file("core/base/src/commonMain/kotlin"),
            rootProject.file("feature/create/src/commonMain/kotlin"),
            rootProject.file("feature/detail/src/commonMain/kotlin"),
            rootProject.file("feature/history/src/commonMain/kotlin"),
            rootProject.file("feature/scan/src/commonMain/kotlin"),
            rootProject.file("feature/settings/src/commonMain/kotlin")
        )
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

        sourceDirs.filter { it.exists() }.forEach { sourceDir ->
            sourceDir.walkTopDown()
                .filter { it.isFile && it.extension == "kt" }
                .forEach { file ->
                    file.readLines().forEachIndexed { lineNum, line ->
                        // Skip if line contains exclude patterns
                        if (excludePatterns.any { line.contains(it) }) return@forEachIndexed

                        patterns.forEach { pattern ->
                            if (pattern.containsMatchIn(line)) {
                                val match = pattern.find(line)?.value ?: ""
                                if (!match.matches(""".*"[\p{So}\p{Sc}]+".*""".toRegex())) {
                                    violations.add("${file.relativeTo(sourceDir)}:${lineNum + 1}: $line")
                                }
                            }
                        }
                    }
                }
        }

        if (violations.isNotEmpty()) {
            println("\n⚠️  Found ${violations.size} potential hardcoded strings:")
            violations.forEach { println("  $it") }
            println("\nConsider using stringResource(Res.string.xxx) for localization.\n")
        } else {
            println("✅ No hardcoded strings detected.")
        }
    }
}

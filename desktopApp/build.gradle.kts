import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    jvm("desktop") {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }

    sourceSets {
        val desktopMain by getting {
            dependencies {
                implementation(project(":shared"))

                // Compose Desktop
                implementation(compose.desktop.currentOs)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)

                // Koin
                implementation(libs.koin.core)

                // Coroutines (swing provides Main dispatcher for desktop)
                implementation(libs.kotlinx.coroutines.core)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.3")

                // Logging
                implementation(libs.napier)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "cut.the.crap.qreverywhere.desktop.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "QrEveryWhere"
            packageVersion = "1.0.0"

            macOS {
                iconFile.set(project.file("src/main/resources/icon.icns"))
            }

            windows {
                iconFile.set(project.file("src/main/resources/icon.ico"))
            }

            linux {
                iconFile.set(project.file("src/main/resources/icon.png"))
            }
        }
    }
}

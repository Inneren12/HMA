import org.gradle.api.JavaVersion

plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose") version "2.0.21"
    id("com.android.library")
    id("org.jetbrains.compose") version "1.7.0"
}

kotlin {
    // Android-таргет для KMP
    androidTarget {
        compilations.all {
            // делаем Kotlin такой же, как Java – 21
            kotlinOptions.jvmTarget = "21"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // в S1 можно вообще без зависимостей, но оставим базовый compose
                implementation(compose.runtime)
                implementation(compose.ui)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("androidx.core:core-ktx:1.13.1")
                implementation("androidx.exifinterface:exifinterface:1.3.7")
                implementation("androidx.compose.ui:ui-graphics:1.7.4")
            }
        }

        val androidUnitTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.13.2")
            }
        }
    }
}

android {
    // ДОЛЖЕН совпадать с package в Kotlin: dev.handmade.core.io
    namespace = "dev.handmade.core.io"

    compileSdk = 34

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        // Kotlin мы подняли до 21 – Java тоже 21
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

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
                implementation(compose.runtime)
                implementation(compose.ui)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("androidx.exifinterface:exifinterface:1.3.7")
                implementation("androidx.core:core-ktx:1.13.1")
                implementation("androidx.compose.ui:ui-graphics:1.7.4")
            }
        }

        val androidUnitTest by getting {
            dependencies {
                // стандартный junit
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.13.2")
                // вот эти три и дают Robolectric + AndroidX test
                implementation("org.robolectric:robolectric:4.12.2")
                implementation("androidx.test:core:1.6.1")
            }
        }
    }
}

android {
    // теперь namespace совпадает с package core.io
    namespace = "core.io"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    testOptions {
        // Robolectric любит ресурсы
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    testImplementation(libs.androidx.core)
}

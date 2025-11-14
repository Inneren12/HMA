import org.gradle.api.JavaVersion

plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose") version "2.0.21"
    id("com.android.library")
    id("org.jetbrains.compose") version "1.7.0"
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.ui)
                implementation(compose.runtime)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("androidx.exifinterface:exifinterface:1.3.7")
                implementation("androidx.core:core-ktx:1.13.1")
                implementation("androidx.compose.ui:ui-graphics:1.7.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation("junit:junit:4.13.2")
                implementation("org.robolectric:robolectric:4.12.2")
                implementation("androidx.test:core:1.6.1")
            }
        }
    }
}

android {
    namespace = "core.io"
    compileSdk = 34
    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

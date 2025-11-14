plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

kotlin {
    // For S1 we limit to Android actual to simplify; other platforms will be added later.
    androidTarget()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":core:domain"))
            }
        }
        val commonTest by getting
        val androidMain by getting
        val androidUnitTest by getting
    }
}

android {
    namespace = "dev.handmade.core.io"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
}

plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

kotlin {
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
    namespace = "dev.handmade.core.decision"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
}

plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "dev.handmade.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "dev.handmade.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:decision"))
    implementation(project(":core:io"))
}

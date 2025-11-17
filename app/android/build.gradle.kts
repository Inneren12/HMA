plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "dev.handmade.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "dev.handmade.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"
    }

    compileOptions {
        // поднимаем Java до того же уровня, что и Kotlin (21)
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
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

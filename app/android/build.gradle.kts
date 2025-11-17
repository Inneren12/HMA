plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("plugin.compose") version "2.0.21"
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

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:decision"))
    implementation(project(":core:io"))

    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui:1.7.4")
    implementation("androidx.compose.foundation:foundation:1.7.4")
    implementation("androidx.compose.material3:material3:1.3.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
}

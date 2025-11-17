plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

kotlin {
    androidTarget()

    sourceSets {
        val commonMain by getting
        val commonTest by getting
        val androidMain by getting
        val androidUnitTest by getting
    }
}

android {
    namespace = "dev.handmade.core.domain"
    compileSdk = 34
    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        // поднимаем Java до того же уровня, что и Kotlin (21)
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

}

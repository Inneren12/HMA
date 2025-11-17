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
        minSdk = 26
    }

    compileOptions {
        // поднимаем Java до того же уровня, что и Kotlin (21)
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

}

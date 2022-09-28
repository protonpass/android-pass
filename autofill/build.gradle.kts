plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    compileSdk = Config.compileSdk
    buildFeatures.buildConfig = false


    defaultConfig {
        minSdk = 27
        targetSdk = Config.targetSdk
    }
}

dependencies {
    api(project(":autofill:service"))
}

plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    compileSdk = Config.compileSdk

    defaultConfig {
        minSdk = Config.minSdk
        targetSdk = Config.targetSdk
        testInstrumentationRunner = Config.testInstrumentationRunner
    }
}

dependencies {
    implementation(Dependencies.passDomainLibs)
    implementation(project(":passwordManager:domain"))
}

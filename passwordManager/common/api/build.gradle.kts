plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = Config.compileSdk

    defaultConfig {
        minSdk = Config.minSdk
        targetSdk = Config.targetSdk
    }
}

dependencies {
    implementation(libs.core.network)
    implementation(libs.kotlinx.coroutines.core)
}

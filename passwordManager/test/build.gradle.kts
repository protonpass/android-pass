plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
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
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.core.account)
    implementation(libs.core.accountManager)
    implementation(libs.core.crypto)
    implementation(libs.core.domain)
    implementation(libs.core.key)
    implementation(libs.core.user)
    implementation(libs.core.utilKotlin)
    implementation(project(":passwordManager:common:api"))
    implementation(project(":passwordManager:domain"))
    implementation(project(":passwordManager:notifications:api"))
}

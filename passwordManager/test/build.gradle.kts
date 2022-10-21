plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
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

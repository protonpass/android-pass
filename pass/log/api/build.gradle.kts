plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "proton.android.pass.log.api"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
    }
}

dependencies {
    implementation(libs.core.network.domain)
    implementation(libs.core.userSettings.domain)
    implementation(libs.core.utilKotlin)
    implementation(libs.timber)
}

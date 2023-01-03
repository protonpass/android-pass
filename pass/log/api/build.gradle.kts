plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "me.proton.android.pass.log.api"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
    }
}

dependencies {
    api(libs.core.userSettings.domain)
    api(libs.core.utilKotlin)
    implementation(libs.timber)
}

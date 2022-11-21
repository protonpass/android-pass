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
    implementation(libs.coroutines.test)
    implementation(libs.junit)
    implementation(libs.core.account)
    implementation(libs.core.accountManager)
    implementation(libs.core.crypto)
    implementation(libs.core.domain)
    implementation(libs.core.key)
    implementation(libs.core.user)
    implementation(libs.core.utilKotlin)
    implementation(projects.pass.common.api)
    implementation(projects.pass.data.api)
    implementation(projects.pass.domain)
    implementation(projects.pass.notifications.api)
}

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("dagger.hilt.android.plugin")
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    namespace = "me.proton.pass.test"

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.coroutines.test)

    api(libs.androidx.lifecycle.viewmodel.savedstate)
    api(libs.core.account.domain)
    api(libs.core.accountManager.domain)
    api(libs.core.cryptoCommon)
    api(libs.core.domain)
    api(libs.core.key.domain)
    api(libs.core.network.domain)
    api(libs.core.user.domain)
    api(libs.junit)

    api(projects.pass.common.api)
    api(projects.pass.data.api)
    api(projects.pass.domain)

    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.android.compiler)
    kapt(libs.androidx.hilt.compiler)
}

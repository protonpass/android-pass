plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    namespace = "me.proton.pass.test"

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        testInstrumentationRunner = Config.testInstrumentationRunner
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
}

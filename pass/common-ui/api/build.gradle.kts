plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    namespace = "proton.android.pass.commonui.api"

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures.compose = true
    composeOptions.kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
}

dependencies {
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui)
    implementation(libs.core.presentation.compose)
    implementation(libs.kotlinx.datetime)

    debugImplementation(libs.androidx.compose.uiTooling)
    implementation(libs.androidx.compose.uiToolingPreview)

    implementation(projects.pass.common.api)
    implementation(projects.pass.commonUiModels.api)
    implementation(projects.pass.crypto.api)
    implementation(projects.pass.data.api)
    implementation(projects.pass.domain)
    implementation(projects.pass.log.api)
    implementation(projects.pass.protos)

    testImplementation(libs.truth)
    testImplementation(libs.kotlinTest)
    testImplementation(libs.junit)

    testImplementation(projects.pass.commonUiModels.fakes)
}

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("dagger.hilt.android.plugin")
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    namespace = "me.proton.pass.autofill.e2e"

    defaultConfig {
        applicationId = "me.proton.pass.core.autofill.e2e"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = Config.versionCode
        versionName = Config.versionName
        testInstrumentationRunner = Config.testInstrumentationRunner
    }

    flavorDimensions += "default"
    productFlavors {
        maybeCreate("dev")
        maybeCreate("alpha")
        maybeCreate("prod")
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
    }
}

dependencies {
    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundationLayout)
    implementation(libs.androidx.compose.foundationLayout)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.material)

    implementation(projects.pass.autofill.impl)
    implementation(projects.pass.data.fakes)
    implementation(projects.pass.biometry.fakes)
    implementation(projects.pass.preferences.fakes)
    implementation(projects.pass.notifications.fakes)
    implementation(projects.pass.clipboard.fakes)
    implementation(projects.pass.commonTest)

    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.android.compiler)
    kapt(libs.androidx.hilt.compiler)
}

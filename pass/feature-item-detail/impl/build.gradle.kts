plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "proton.android.pass.featureitemdetail.impl"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
    }
}

dependencies {
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.core.accountManager.domain)
    implementation(libs.core.presentation)
    implementation(libs.core.presentation.compose)
    implementation(libs.kotlinx.collections)
    implementation(libs.kotlinx.datetime)

    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.android.compiler)
    kapt(libs.androidx.hilt.compiler)

    debugImplementation(libs.showkase)
    kspDebug(libs.showkaseProcessor)

    implementation(projects.pass.clipboard.api)
    implementation(projects.pass.common.api)
    implementation(projects.pass.commonUi.api)
    implementation(projects.pass.commonUiModels.api)
    implementation(projects.pass.composeComponents.impl)
    implementation(projects.pass.crypto.api)
    implementation(projects.pass.data.api)
    implementation(projects.pass.domain)
    implementation(projects.pass.log.api)
    implementation(projects.pass.navigation.api)
    implementation(projects.pass.notifications.api)
    implementation(projects.pass.preferences.api)
    implementation(projects.pass.telemetry.api)
    implementation(projects.pass.totp.api)

    // Temporarily depend on the impl module until move the bottomsheets to routes
    implementation(projects.pass.featureTrash.impl)

    debugImplementation(libs.androidx.compose.uiTooling)
    implementation(libs.androidx.compose.uiToolingPreview)
    debugImplementation(libs.androidx.compose.uiTestManifest)

    testImplementation(libs.turbine)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinTest)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.datetime)
    testImplementation(projects.pass.commonTest)
    testImplementation(projects.pass.crypto.fakes)
    testImplementation(projects.pass.data.fakes)
    testImplementation(projects.pass.notifications.fakes)
    testImplementation(projects.pass.preferences.fakes)
    testImplementation(projects.pass.telemetry.fakes)
}

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("dagger.hilt.android.plugin")
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "proton.android.pass.commonpresentation.impl"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

androidComponents.beforeVariants { variant ->
    variant.enableAndroidTest = false
}

dependencies {
    api(projects.pass.commonPresentation.api)

    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.core.account.domain)
    implementation(libs.core.domain)
    implementation(libs.core.user.domain)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime)

    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.android.compiler)
    kapt(libs.androidx.hilt.compiler)

    implementation(projects.pass.biometry.api)
    implementation(projects.pass.clipboard.api)
    implementation(projects.pass.common.api)
    implementation(projects.pass.commonRust.api)
    implementation(projects.pass.commonUi.api)
    implementation(projects.pass.commonUiModels.api)
    implementation(projects.pass.crypto.api)
    implementation(projects.pass.data.api)
    implementation(projects.pass.domain)
    implementation(projects.pass.log.api)
    implementation(projects.pass.notifications.api)
    implementation(projects.pass.preferences.api)
    implementation(projects.pass.securityCenter.api)
    implementation(projects.pass.totp.api)
}

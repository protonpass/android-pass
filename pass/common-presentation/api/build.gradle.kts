plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
}

android {
    namespace = "proton.android.pass.commonpresentation.api"
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
    implementation(libs.androidx.compose.runtime)
    implementation(libs.core.account.domain)
    implementation(libs.core.domain)
    implementation(libs.core.user.domain)
    implementation(libs.kotlinx.coroutines.core)

    implementation(projects.pass.common.api)
    implementation(projects.pass.commonUi.api)
    implementation(projects.pass.commonUiModels.api)
    implementation(projects.pass.crypto.api)
    implementation(projects.pass.domain)
    implementation(projects.pass.notifications.api)
    implementation(projects.pass.preferences.api)
    implementation(projects.pass.totp.api)
}

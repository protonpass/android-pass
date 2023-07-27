plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("dagger.hilt.android.plugin")
    id("org.jetbrains.kotlin.kapt")
}

val appVersionName: String = "1.7.2"
val appVersionCode: Int = versionCode(appVersionName)
val archivesBaseName = "AutofillE2EApp-$appVersionName"

fun versionCode(versionName: String): Int {
    val segment = versionName.split('.').map { it.toInt() }
    return (segment[0] * 10000000) + (segment[1] * 100000) + (segment[2] * 1000)
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    namespace = "proton.android.pass.autofill.e2e"

    defaultConfig {
        applicationId = "proton.android.pass.autofill.e2e"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = appVersionCode
        versionName = appVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    flavorDimensions += "version"
    productFlavors {
        maybeCreate("dev")
        maybeCreate("alpha")
        maybeCreate("play")
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
    }
}

androidComponents.beforeVariants { variant ->
    variant.enableAndroidTest = false
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
    implementation(libs.kotlinx.datetime)
    implementation(libs.material)
    implementation(libs.kotlinx.datetime)
    implementation(libs.core.userSettings.domain)

    implementation(projects.pass.account.fakes)
    implementation(projects.pass.autofill.impl)
    implementation(projects.pass.biometry.fakes)
    implementation(projects.pass.clipboard.fakes)
    implementation(projects.pass.common.fakes)
    implementation(projects.pass.commonUi.fakes)
    implementation(projects.pass.commonTest)
    implementation(projects.pass.crypto.fakes)
    implementation(projects.pass.data.api)
    implementation(projects.pass.data.fakes)
    implementation(projects.pass.domain)
    implementation(projects.pass.notifications.fakes)
    implementation(projects.pass.preferences.fakes)
    implementation(projects.pass.telemetry.fakes)
    implementation(projects.pass.totp.fakes)

    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.android.compiler)
    kapt(libs.androidx.hilt.compiler)
}

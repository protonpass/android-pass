plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("dagger.hilt.android.plugin")
    id("org.jetbrains.kotlin.kapt")
    id("androidx.baselineprofile")
    alias(libs.plugins.gradlePlugin.compose.compiler)
}

val appVersionName: String = "1.32.6"
val appVersionCode: Int = versionCode(appVersionName)
val archivesBaseName = "FeatureHomeApp-$appVersionName"

fun versionCode(versionName: String): Int {
    val segment = versionName.split('.').map { it.toInt() }
    return (segment[0] * 10000000) + (segment[1] * 100000) + (segment[2] * 1000)
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    namespace = "proton.android.pass.features.homedemo"

    defaultConfig {
        applicationId = "proton.android.pass.features.homedemo"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = appVersionCode
        versionName = appVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        proguardFiles.add(file("benchmark.pro"))

        buildFeatures {
            buildConfig = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    buildFeatures {
        compose = true
    }

    buildTypes {
        create("benchmarkRelease") {
            signingConfig = signingConfigs.getByName("debug")
        }
        create("nonMinifiedRelease") {
            signingConfig = signingConfigs.getByName("debug")
        }
    }
}

androidComponents.beforeVariants { variant ->
    variant.enableAndroidTest = false
}

dependencies {
    implementation(libs.accompanist.navigation.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundationLayout)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.androidx.startup.runtime)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.collections)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.material)
    implementation(libs.timber)

    implementation(libs.core.presentation)
    implementation(libs.core.presentation.compose)
    implementation(libs.core.userSettings.domain)

    implementation(projects.pass.account.fakes)
    implementation(projects.pass.appConfig.fakes)
    implementation(projects.pass.autofill.fakes)
    implementation(projects.pass.biometry.fakes)
    implementation(projects.pass.clipboard.fakes)
    implementation(projects.pass.common.fakes)
    implementation(projects.pass.commonRust.fakes)
    implementation(projects.pass.commonTest)
    implementation(projects.pass.commonUi.api)
    implementation(projects.pass.commonUi.impl)
    implementation(projects.pass.composeComponents.impl)
    implementation(projects.pass.crypto.fakes)
    implementation(projects.pass.data.api)
    implementation(projects.pass.data.fakes)
    implementation(projects.pass.domain)
    implementation(projects.pass.inAppReview.fakes)
    implementation(projects.pass.features.home)
    implementation(projects.pass.searchOptions.fakes)
    implementation(projects.pass.navigation.api)
    implementation(projects.pass.notifications.fakes)
    implementation(projects.pass.telemetry.fakes)
    implementation(projects.pass.totp.fakes)
    implementation(projects.pass.preferences.api)
    implementation(projects.pass.preferences.fakes)
    implementation(projects.pass.securityCenter.fakes)

    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.android.compiler)
    kapt(libs.androidx.hilt.compiler)
}

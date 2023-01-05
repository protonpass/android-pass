plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "me.proton.android.pass.navigation.api"
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
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.accompanist.navigation.animation)
    implementation(libs.androidx.compose.ui)
    implementation(libs.core.cryptoAndroid)
    implementation(libs.core.cryptoCommon)
    implementation(libs.core.utilKotlin)
    implementation(libs.androidx.navigation.compose)

    api(libs.androidx.compose.runtime)

    implementation(projects.pass.log.api)
}

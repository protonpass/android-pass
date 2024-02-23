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
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.core.account.domain)
    implementation(libs.core.domain)
    implementation(libs.core.user.domain)

    implementation(projects.pass.common.api)
    implementation(projects.pass.commonUiModels.api)
    implementation(projects.pass.domain)
}

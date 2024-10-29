plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("dagger.hilt.android.plugin")
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "proton.android.pass.data.fakes"
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
    api(projects.pass.data.api)

    implementation(libs.core.user.domain)
    implementation(libs.core.userSettings.domain)
    implementation(libs.kotlinx.collections)
    implementation(libs.kotlinx.datetime)
    implementation(projects.pass.common.api)
    implementation(projects.pass.commonTest)
    implementation(projects.pass.commonRust.api)
    implementation(projects.pass.crypto.api)
    implementation(projects.pass.crypto.fakes)
    implementation(projects.pass.dataModels.api)
    implementation(projects.pass.domain)
    implementation(projects.pass.protos)

    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.android.compiler)
    kapt(libs.androidx.hilt.compiler)
}

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("dagger.hilt.android.plugin")
    id("org.jetbrains.kotlin.kapt")
}


android {
    namespace = "proton.android.pass.image.impl"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
    }
}

androidComponents.beforeVariants { variant ->
    variant.enableAndroidTest = false
}

dependencies {
    implementation(libs.coil.base)
    implementation(libs.coil.gif)
    implementation(libs.coil.svg)
    implementation(libs.core.utilKotlin)
    implementation(libs.kotlinx.datetime)

    implementation(projects.pass.commonUi.api)
    implementation(projects.pass.crypto.api)
    implementation(projects.pass.data.api)
    implementation(projects.pass.domain)
    implementation(projects.pass.image.api)
    implementation(projects.pass.log.api)

    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.android.compiler)
    kapt(libs.androidx.hilt.compiler)
}

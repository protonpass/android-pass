plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("dagger.hilt.android.plugin")
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "proton.android.pass.log.fakes"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

    }

    flavorDimensions += "version"
    productFlavors {
        maybeCreate("dev")
        maybeCreate("alpha")
        maybeCreate("play")
        maybeCreate("fdroid")
        maybeCreate("quest")
    }

}

androidComponents.beforeVariants { variant ->
    variant.enableAndroidTest = false
}

dependencies {
    api(projects.pass.log.api)
    api(projects.pass.log.impl)

    implementation(libs.core.account.domain)
    implementation(libs.core.domain)
    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.dagger.hilt.android)
    implementation(libs.dagger.hilt.android.testing)
    kapt(libs.dagger.hilt.android.compiler)
    kapt(libs.androidx.hilt.compiler)
}

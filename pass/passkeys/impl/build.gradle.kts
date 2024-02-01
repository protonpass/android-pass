plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("dagger.hilt.android.plugin")
    kotlin("plugin.serialization")
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "proton.android.pass.passkeys.impl"
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
    api(projects.pass.passkeys.api)

    compileOnly(files("../../passkeys/impl/libs/lib-release.aar"))
    implementation("net.java.dev.jna:jna:5.13.0@aar")

    implementation(libs.core.utilKotlin)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.android.compiler)
    kapt(libs.androidx.hilt.compiler)

    implementation(projects.pass.domain)
}

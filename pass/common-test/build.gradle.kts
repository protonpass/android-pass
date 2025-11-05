plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("dagger.hilt.android.plugin")
    id("org.jetbrains.kotlin.kapt")
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    namespace = "proton.android.pass.test"

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
    implementation(libs.coroutines.test)
    implementation(libs.kotlinx.datetime)

    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    implementation(libs.junit)
    implementation(libs.core.user.domain)
    implementation(libs.core.crypto)

    implementation(projects.pass.common.api)
    implementation(projects.pass.data.api)
    implementation(projects.pass.dataModels.api)
    implementation(projects.pass.domain)
    implementation(projects.pass.account.fakes)
    implementation(projects.pass.crypto.fakes)
    implementation(projects.pass.protos)
    implementation(projects.pass.network.fakes)

    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.android.compiler)
    kapt(libs.androidx.hilt.compiler)
    api(libs.bundles.test.android) {
        exclude(module = "protobuf-lite")
    }
}

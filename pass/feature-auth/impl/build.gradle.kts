plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
}

android {
    namespace = "proton.android.pass.featureauth.impl"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()

        testInstrumentationRunner = "proton.android.pass.test.HiltRunner"
    }
    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
    }

    testOptions {
        managedDevices {
            devices {
                maybeCreate<com.android.build.api.dsl.ManagedVirtualDevice>("pixel2api30").apply {
                    device = "Pixel 2"
                    apiLevel = 30
                    systemImageSource = "aosp-atd"
                }
            }
        }
    }
}

dependencies {

    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui)
    debugImplementation(libs.androidx.compose.uiTooling)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(projects.pass.biometry.api)
    implementation(projects.pass.composeComponents.impl)
    implementation(projects.pass.preferences.api)
    implementation(projects.pass.navigation.api)
    implementation(projects.pass.log.api)

    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.android.compiler)
    kapt(libs.androidx.hilt.compiler)

    debugImplementation(libs.showkase)
    kspDebug(libs.showkaseProcessor)

    testImplementation(projects.pass.commonTest)
    testImplementation(projects.pass.biometry.fakes)
    testImplementation(projects.pass.preferences.fakes)

    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.truth)

    kaptAndroidTest(libs.dagger.hilt.android.compiler)
    androidTestImplementation(projects.pass.commonTest)
    androidTestImplementation(projects.pass.preferences.fakes)
    androidTestImplementation(projects.pass.biometry.fakes)
}

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
}

android {
    namespace = "proton.android.pass.featureaccount.impl"
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
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.core.presentation)
    implementation(libs.core.presentation.compose)
    implementation(libs.core.user.domain)

    debugImplementation(libs.androidx.compose.uiTooling)
    implementation(libs.androidx.compose.uiToolingPreview)
    debugImplementation(libs.androidx.compose.uiTestManifest)

    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.android.compiler)
    kapt(libs.androidx.hilt.compiler)

    debugImplementation(libs.showkase)
    kspDebug(libs.showkaseProcessor)

    implementation(projects.pass.domain)
    implementation(projects.pass.commonUi.api)
    implementation(projects.pass.composeComponents.impl)
    implementation(projects.pass.data.api)
    implementation(projects.pass.navigation.api)
    implementation(projects.pass.notifications.api)
    implementation(projects.pass.log.api)

    kaptAndroidTest(libs.dagger.hilt.android.compiler)
    androidTestImplementation(projects.pass.commonTest)
    androidTestImplementation(projects.pass.data.fakes)
    androidTestImplementation(projects.pass.notifications.fakes)

    testImplementation(projects.pass.commonTest)
    testImplementation(projects.pass.data.fakes)
    testImplementation(projects.pass.notifications.fakes)
    testImplementation(libs.truth)
    testImplementation(libs.turbine)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.junit)
}

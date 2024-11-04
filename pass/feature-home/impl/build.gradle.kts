plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin")
    id("org.jetbrains.kotlin.kapt")
    id("androidx.baselineprofile")
}

android {
    namespace = "proton.android.pass.featurehome.impl"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        

        testInstrumentationRunner = "proton.android.pass.test.HiltRunner"
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
    implementation(libs.accompanist.permissions)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.core.presentation)
    implementation(libs.core.presentation.compose)
    implementation(libs.core.user.domain)
    implementation(libs.kotlinx.collections)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)

    debugImplementation(libs.androidx.compose.uiTooling)
    implementation(libs.androidx.compose.uiToolingPreview)

    debugImplementation(libs.showkase)
    kspDebug(libs.showkaseProcessor)

    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.android.compiler)
    kapt(libs.androidx.hilt.compiler)

    implementation(projects.pass.appConfig.api)
    implementation(projects.pass.autofill.api)
    implementation(projects.pass.biometry.api)
    implementation(projects.pass.clipboard.api)
    implementation(projects.pass.common.api)
    implementation(projects.pass.commonPresentation.api)
    implementation(projects.pass.commonUi.api)
    implementation(projects.pass.commonUiModels.api)
    implementation(projects.pass.composeComponents.impl)
    implementation(projects.pass.crypto.api)
    implementation(projects.pass.data.api)
    implementation(projects.pass.domain)
    implementation(projects.pass.log.api)
    implementation(projects.pass.navigation.api)
    implementation(projects.pass.notifications.api)
    implementation(projects.pass.preferences.api)
    implementation(projects.pass.telemetry.api)
    implementation(projects.pass.searchOptions.api)

    // Temporarily depend on the impl module until move the bottomsheets to routes
    implementation(projects.pass.features.trash)

    debugImplementation(libs.androidx.compose.uiTooling)
    debugImplementation(libs.androidx.compose.uiTestManifest)

    testImplementation(libs.turbine)
    testImplementation(libs.truth)
    testImplementation(libs.coroutines.test)

    testImplementation(projects.pass.appConfig.fakes)
    testImplementation(projects.pass.autofill.fakes)
    testImplementation(projects.pass.biometry.fakes)
    testImplementation(projects.pass.clipboard.fakes)
    testImplementation(projects.pass.common.fakes)
    testImplementation(projects.pass.commonUi.fakes)
    testImplementation(projects.pass.commonTest)
    testImplementation(projects.pass.crypto.fakes)
    testImplementation(projects.pass.data.fakes)
    testImplementation(projects.pass.notifications.fakes)
    testImplementation(projects.pass.preferences.fakes)
    testImplementation(projects.pass.telemetry.fakes)
    testImplementation(projects.pass.searchOptions.fakes)

    kaptAndroidTest(libs.dagger.hilt.android.compiler)
    androidTestImplementation(libs.kotlinTest)
    androidTestImplementation(libs.truth)
    androidTestImplementation(projects.pass.appConfig.fakes)
    androidTestImplementation(projects.pass.autofill.fakes)
    androidTestImplementation(projects.pass.common.fakes)
    androidTestImplementation(projects.pass.commonUi.fakes)
    androidTestImplementation(projects.pass.commonTest)
    androidTestImplementation(projects.pass.biometry.fakes)
    androidTestImplementation(projects.pass.clipboard.fakes)
    androidTestImplementation(projects.pass.crypto.fakes)
    androidTestImplementation(projects.pass.data.fakes)
    androidTestImplementation(projects.pass.notifications.fakes)
    androidTestImplementation(projects.pass.preferences.fakes)
    androidTestImplementation(projects.pass.telemetry.fakes)
    androidTestImplementation(projects.pass.searchOptions.fakes)
    androidTestImplementation(projects.pass.securityCenter.fakes)

    baselineProfile(projects.pass.featureHome.macrobenchmark)
}

kapt {
    correctErrorTypes = true
}

baselineProfile {
    filter {
        include("proton.android.pass.featurehome.impl.**")
    }
}

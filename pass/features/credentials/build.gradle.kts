plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin")
    id("org.jetbrains.kotlin.kapt")
    id("org.jetbrains.kotlin.plugin.serialization")
    alias(libs.plugins.gradlePlugin.compose.compiler)
}

android {
    namespace = "proton.android.pass.features.credentials"
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

androidComponents.beforeVariants { variant ->
    variant.enableAndroidTest = false
}

dependencies {
    implementation(libs.accompanist.navigation.material)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.core.accountManager.domain)
    implementation(libs.core.presentation)
    implementation(libs.core.presentation.compose)
    implementation(libs.core.utilKotlin)
    implementation(libs.dagger.hilt.android)
    implementation(libs.kotlinx.collections)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)

    debugImplementation(libs.androidx.compose.uiTooling)
    implementation(libs.androidx.compose.uiToolingPreview)

    debugImplementation(libs.showkase)
    kspDebug(libs.showkaseProcessor)

    kapt(libs.dagger.hilt.android.compiler)
    kapt(libs.androidx.hilt.compiler)

    implementation(projects.pass.biometry.api)
    implementation(projects.pass.common.api)
    implementation(projects.pass.data.api)
    implementation(projects.pass.domain)
    implementation(projects.pass.log.api)
    implementation(projects.pass.telemetry.api)

    testImplementation(libs.truth)
    testImplementation(libs.kotlinTest)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.junit)
    testImplementation(libs.turbine)
}

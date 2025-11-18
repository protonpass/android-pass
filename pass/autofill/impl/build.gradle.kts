plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("dagger.hilt.android.plugin")
    id("kotlin-parcelize")
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.kapt")
    alias(libs.plugins.gradlePlugin.compose.compiler)
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    namespace = "proton.android.pass.autofill.service"

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        testInstrumentationRunner = "proton.android.pass.test.HiltRunner"
    }
    flavorDimensions += "version"
    productFlavors {
        maybeCreate("dev")
        maybeCreate("alpha")
        maybeCreate("play")
        maybeCreate("fdroid")
        maybeCreate("quest")
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    testOptions {
        managedDevices {
            allDevices {
                maybeCreate<com.android.build.api.dsl.ManagedVirtualDevice>("pixel2api30").apply {
                    device = "Pixel 2"
                    apiLevel = 30
                    systemImageSource = "aosp-atd"
                }
            }
        }
    }
}

configurations {
    // Remove duplicate classes (keep "org.jetbrains").
    implementation.get().exclude(mapOf("group" to "com.intellij", "module" to "annotations"))
    implementation.get().exclude(mapOf("group" to "org.intellij", "module" to "annotations"))
}

dependencies {
    api(projects.pass.autofill.api)

    implementation(libs.accompanist.navigation.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.autofill)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundationLayout)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.core.accountManager.domain)
    implementation(libs.core.presentation)
    implementation(libs.core.presentation.compose)
    implementation(libs.core.utilKotlin)
    implementation(libs.kotlinx.collections)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.android.compiler)
    kapt(libs.androidx.hilt.compiler)

    implementation(projects.pass.account.api)
    implementation(projects.pass.biometry.api)
    implementation(projects.pass.clipboard.api)
    implementation(projects.pass.common.api)
    implementation(projects.pass.commonPresentation.api)
    implementation(projects.pass.commonPresentation.impl)
    implementation(projects.pass.commonUi.api)
    implementation(projects.pass.commonUi.impl)
    implementation(projects.pass.commonUiModels.api)
    implementation(projects.pass.composeComponents.impl)
    implementation(projects.pass.crypto.api)
    implementation(projects.pass.data.api)
    implementation(projects.pass.domain)
    implementation(projects.pass.features.auth)
    implementation(projects.pass.features.accountSelector)
    implementation(projects.pass.features.itemCreate)
    implementation(projects.pass.searchOptions.api)
    implementation(projects.pass.features.searchOptions)
    implementation(projects.pass.features.password)
    implementation(projects.pass.features.selectItem)
    implementation(projects.pass.features.vault)
    implementation(projects.pass.inAppReview.api)
    implementation(projects.pass.log.api)
    implementation(projects.pass.navigation.api)
    implementation(projects.pass.notifications.api)
    implementation(projects.pass.preferences.api)
    implementation(projects.pass.telemetry.api)
    implementation(projects.pass.totp.api)

    debugImplementation(libs.showkase)
    kspDebug(libs.showkaseProcessor)

    debugImplementation(libs.androidx.compose.uiTooling)
    implementation(libs.androidx.compose.uiToolingPreview)

    testImplementation(projects.pass.biometry.fakes)
    testImplementation(projects.pass.commonTest)
    testImplementation(projects.pass.clipboard.fakes)
    testImplementation(projects.pass.crypto.fakes)
    testImplementation(projects.pass.data.fakes)
    testImplementation(projects.pass.inAppReview.fakes)
    testImplementation(projects.pass.notifications.fakes)
    testImplementation(projects.pass.preferences.fakes)
    testImplementation(projects.pass.telemetry.fakes)
    testImplementation(projects.pass.totp.fakes)

    testImplementation(libs.coroutines.test)
    testImplementation(libs.junit)
    testImplementation(libs.timber)
    testImplementation(libs.truth)
    testImplementation(libs.turbine)
}

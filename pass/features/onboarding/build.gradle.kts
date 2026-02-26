/*
 * Copyright (c) 2026 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin")
    id("org.jetbrains.kotlin.kapt")
    alias(libs.plugins.gradlePlugin.compose.compiler)
}

android {
    namespace = "proton.android.pass.features.onboarding"
    compileSdk = libs.versions.compileSdk.get().toInt()
    
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "proton.android.pass.test.HiltRunner"
    }
    
    buildFeatures {
        compose = true
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

dependencies {
    implementation(libs.accompanist.pager.indicators)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.core.presentation)
    implementation(libs.core.presentation.compose)
    implementation(libs.core.telemetry.presentation)
    implementation(libs.kotlinx.collections)

    debugImplementation(libs.showkase)
    kspDebug(libs.showkaseProcessor)

    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.android.compiler)
    kapt(libs.androidx.hilt.compiler)

    implementation(projects.pass.autofill.api)
    implementation(projects.pass.biometry.api)
    implementation(projects.pass.commonUi.api)
    implementation(projects.pass.composeComponents.impl)
    implementation(projects.pass.data.api)
    implementation(projects.pass.domain)
    implementation(projects.pass.log.api)
    implementation(projects.pass.navigation.api)
    implementation(projects.pass.notifications.api)
    implementation(projects.pass.preferences.api)
    implementation(projects.pass.appConfig.api)

    debugImplementation(libs.androidx.compose.uiTooling)
    implementation(libs.androidx.compose.uiToolingPreview)
    debugImplementation(libs.androidx.compose.uiTestManifest)

    testImplementation(libs.turbine)
    testImplementation(libs.truth)
    testImplementation(libs.coroutines.test)
    testImplementation(projects.pass.autofill.fakes)
    testImplementation(projects.pass.biometry.fakes)
    testImplementation(projects.pass.commonTest)
    testImplementation(projects.pass.data.fakes)
    testImplementation(projects.pass.notifications.fakes)
    testImplementation(projects.pass.preferences.fakes)

    kaptAndroidTest(libs.dagger.hilt.android.compiler)
    androidTestImplementation(projects.pass.commonTest)
    androidTestImplementation(projects.pass.account.fakes)
    androidTestImplementation(projects.pass.autofill.fakes)
    androidTestImplementation(projects.pass.biometry.fakes)
    androidTestImplementation(projects.pass.data.fakes)
    androidTestImplementation(projects.pass.notifications.fakes)
    androidTestImplementation(projects.pass.preferences.fakes)
    androidTestImplementation(projects.pass.securityCenter.fakes)
    androidTestImplementation(projects.pass.appConfig.fakes)
}

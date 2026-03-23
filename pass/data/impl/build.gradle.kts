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
    kotlin("plugin.serialization")
    id("dagger.hilt.android.plugin")
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "proton.android.pass.data.impl"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    sourceSets {
        getByName("androidTest").assets.srcDir("$projectDir/schemas")
        getByName("test").java.srcDir("src/sharedTest/kotlin")
        getByName("androidTest").java.srcDir("src/sharedTest/kotlin")
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

kapt {
    arguments {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
}

dependencies {

    api(projects.pass.data.api)

    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.core.account)
    implementation(libs.core.accountManager)
    implementation(libs.core.challenge)
    implementation(libs.core.crypto)
    implementation(libs.core.data)
    implementation(libs.core.dataRoom)
    implementation(libs.core.domain)
    implementation(libs.core.eventManager)
    implementation(libs.core.featureFlag)
    implementation(libs.core.humanVerification)
    implementation(libs.core.key)
    implementation(libs.core.keyTransparency)
    implementation(libs.core.network)
    implementation(libs.core.notification)
    implementation(libs.core.observability)
    implementation(libs.core.payment)
    implementation(libs.core.push)
    implementation(libs.core.telemetry.data)
    implementation(libs.core.telemetry.domain)
    implementation(libs.core.report.domain)
    implementation(libs.core.user)
    implementation(libs.core.userRecovery)
    implementation(libs.core.userSettings)
    implementation(libs.core.utilKotlin)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.collections)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit)

    kapt(libs.androidx.room.compiler)

    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.android.compiler)
    kapt(libs.androidx.hilt.compiler)

    implementation(projects.pass.appConfig.api)
    implementation(projects.pass.crypto.api)
    implementation(projects.pass.common.api)
    implementation(projects.pass.commonRust.api)
    implementation(projects.pass.dataModels.api)
    implementation(projects.pass.domain)
    implementation(projects.pass.files.api)
    implementation(projects.pass.log.api)
    implementation(projects.pass.network.api)
    implementation(projects.pass.notifications.api)
    implementation(projects.pass.preferences.api)
    implementation(projects.pass.protos)
    implementation(projects.pass.securityCenter.api)
    implementation(projects.pass.telemetry.api)

    testImplementation(libs.core.test.kotlin)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinTest)
    testImplementation(libs.truth)
    testImplementation(libs.turbine)

    testImplementation(projects.pass.account.fakes)
    testImplementation(projects.pass.appConfig.fakes)
    testImplementation(projects.pass.common.fakes)
    testImplementation(projects.pass.commonTest)
    testImplementation(projects.pass.crypto.fakes)
    testImplementation(projects.pass.data.fakes)
    testImplementation(projects.pass.network.fakes)
    testImplementation(projects.pass.notifications.fakes)
    testImplementation(projects.pass.preferences.fakes)

    androidTestImplementation(projects.pass.account.fakes)
    androidTestImplementation(projects.pass.commonTest)
    androidTestImplementation(projects.pass.common.fakes)
    androidTestImplementation(projects.pass.crypto.fakes)
    androidTestImplementation(projects.pass.data.fakes)
    androidTestImplementation(projects.pass.network.fakes)
    androidTestImplementation(projects.pass.preferences.fakes)

    androidTestImplementation(libs.androidx.work.testing)
    androidTestImplementation(libs.truth)

    androidTestImplementation(libs.androidx.compose.ui.test)
    androidTestImplementation(libs.androidx.compose.ui.test.junit)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.core.ktx)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.kotlinTest)
    androidTestImplementation(libs.core.test.android.instrumented) {
        // Exclude protobuf, as it would clash with our protobuf library
        exclude("com.google.protobuf")
    }
}

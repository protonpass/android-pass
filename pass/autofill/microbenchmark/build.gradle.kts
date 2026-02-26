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

@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    namespace = "proton.android.pass.autofill.microbenchmark"

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        missingDimensionStrategy("version", "play")
        missingDimensionStrategy("env", "prod")

        testInstrumentationRunner = "androidx.benchmark.junit4.AndroidBenchmarkRunner"
        testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] = "EMULATOR,LOW-BATTERY"
        testInstrumentationRunnerArguments["androidx.benchmark.profiling.mode"] = "MethodTracing"
    }

    buildTypes.forEach {
        it.isMinifyEnabled = false
        it.isTestCoverageEnabled = false
        it.isDebuggable = false
        it.isProfileable = true
    }

    buildTypes {
        getByName("release") {
            // The androidx.benchmark plugin configures release buildType with proper settings, such as:
            // - disables code coverage
            // - adds CPU clock locking task
            // - signs release buildType with debug signing config
            // - copies benchmark results into build/outputs/connected_android_test_additional_output folder
        }
    }

    experimentalProperties["android.experimental.self-instrumenting"] = true
}


androidComponents {
    beforeVariants(selector().all()) {
        it.enable = it.buildType == "release"
    }
}


dependencies {
    androidTestImplementation(projects.pass.autofill.api)
    androidTestImplementation(projects.pass.autofill.impl)

    androidTestImplementation(libs.kotlinx.collections)
    androidTestImplementation(libs.kotlinx.coroutines.core)
    androidTestImplementation(libs.kotlinx.datetime)
    androidTestImplementation(libs.kotlinx.serialization.json)

    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(projects.pass.biometry.fakes)
    androidTestImplementation(projects.pass.clipboard.fakes)
    androidTestImplementation(projects.pass.common.fakes)
    androidTestImplementation(projects.pass.commonRust.fakes)
    androidTestImplementation(projects.pass.commonTest)
    androidTestImplementation(projects.pass.commonUi.fakes)
    androidTestImplementation(projects.pass.crypto.fakes)
    androidTestImplementation(projects.pass.data.fakes)
    androidTestImplementation(projects.pass.searchOptions.fakes)
    androidTestImplementation(projects.pass.inAppReview.fakes)
    androidTestImplementation(projects.pass.notifications.fakes)
    androidTestImplementation(projects.pass.preferences.fakes)
    androidTestImplementation(projects.pass.telemetry.fakes)
    androidTestImplementation(projects.pass.totp.fakes)

    androidTestImplementation(libs.androidx.benchmark.micro)
}

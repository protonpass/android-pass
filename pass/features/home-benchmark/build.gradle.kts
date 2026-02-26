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
    id("com.android.test")
    id("org.jetbrains.kotlin.android")
    id("androidx.baselineprofile")
}

android {
    namespace = "proton.android.pass.features.home.macrobenchmark"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        missingDimensionStrategy("version", "play")
        missingDimensionStrategy("env", "prod")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] = "EMULATOR"
    }

    targetProjectPath = ":pass:features:home-demo"

    testOptions {
        managedDevices {
            allDevices {
                maybeCreate<com.android.build.api.dsl.ManagedVirtualDevice>("pixel2api31").apply {
                    device = "Pixel 2"
                    apiLevel = 31
                    systemImageSource = "aosp"
                }
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.test.espresso.core)
    implementation(libs.androidx.test.junit)
    implementation(libs.androidx.test.uiautomator)
    implementation(libs.androidx.benchmark.macro)

    implementation(projects.pass.commonUi.api)
}

baselineProfile {
    managedDevices += "pixel2api31"
    useConnectedDevices = false
}

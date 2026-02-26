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
    id("kotlin-parcelize")
}

android {
    namespace = "proton.android.pass.commonpresentation.api"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

    }}

androidComponents.beforeVariants { variant ->
    variant.enableAndroidTest = false
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.core.compose)

    implementation(libs.core.account.domain)
    implementation(libs.core.domain)
    implementation(libs.core.user.domain)
    implementation(libs.kotlinx.coroutines.core)

    implementation(projects.pass.common.api)
    implementation(projects.pass.commonUi.api)
    implementation(projects.pass.commonUiModels.api)
    implementation(projects.pass.crypto.api)
    implementation(projects.pass.domain)
    implementation(projects.pass.notifications.api)
    implementation(projects.pass.preferences.api)
    implementation(projects.pass.totp.api)
    implementation(projects.pass.data.api)
    implementation(projects.pass.log.api)

    implementation(libs.kotlinx.collections)
    implementation(libs.core.plan.domain)

    testImplementation(libs.core.test.kotlin)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinTest)
    testImplementation(libs.truth)
    testImplementation(projects.pass.commonTest)
}

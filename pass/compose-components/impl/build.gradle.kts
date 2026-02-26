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
    alias(libs.plugins.gradlePlugin.compose.compiler)
}

android {
    namespace = "proton.android.pass.composecomponents.impl"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        
    }

    buildFeatures {
        compose = true
    }
}

androidComponents.beforeVariants { variant ->
    variant.enableAndroidTest = false
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.core.compose)
    debugImplementation(libs.androidx.compose.uiTooling)

    implementation(libs.accompanist.placeholder)
    implementation(libs.accompanist.navigation.material)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.annotation)

    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.coil.compose)
    implementation(libs.core.presentation)
    implementation(libs.core.presentation.compose)
    implementation(libs.kotlinx.collections)
    implementation(libs.kotlinx.datetime)

    implementation(projects.pass.common.api)
    implementation(projects.pass.commonPresentation.impl)
    implementation(projects.pass.commonUi.api)
    implementation(projects.pass.commonRust.api)
    implementation(projects.pass.commonUiModels.api)
    implementation(projects.pass.domain)
    implementation(projects.pass.searchOptions.api)
    implementation(projects.pass.inAppReview.api)
    implementation(projects.pass.log.api)
    implementation(projects.pass.navigation.api)
    implementation(projects.pass.notifications.api)
    implementation(projects.pass.preferences.api)
    implementation(projects.pass.protos)

    debugImplementation(libs.showkase)
    kspDebug(libs.showkaseProcessor)

    testImplementation(libs.truth)
    testImplementation(libs.kotlinTest)
    testImplementation(libs.junit)
}

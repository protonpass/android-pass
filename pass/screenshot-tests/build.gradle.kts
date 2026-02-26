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
    id("app.cash.paparazzi")
    id("com.google.devtools.ksp")
    alias(libs.plugins.gradlePlugin.compose.compiler)
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    namespace = "proton.android.pass.screenshottests"
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
    flavorDimensions += "version"
    productFlavors {
        maybeCreate("dev")
        maybeCreate("alpha")
        maybeCreate("play")
    }
    buildFeatures {
        compose = true
    }
    sourceSets {
        getByName("test").java.srcDirs("build/generated/ksp/devDebugUnitTest/kotlin")
    }
}

androidComponents {
    beforeVariants(selector().withBuildType("release")) { builder ->
        builder.enable = false
    }
    beforeVariants { variant ->
        variant.enableAndroidTest = false
    }
}

dependencies {
    implementation(projects.pass.autofill.impl)
    implementation(projects.pass.commonUi.api)
    implementation(projects.pass.composeComponents.impl)
    implementation(projects.pass.features.account)
    implementation(projects.pass.features.auth)
    implementation(projects.pass.features.attachments)
    implementation(projects.pass.features.credentials)
    implementation(projects.pass.features.home)
    implementation(projects.pass.features.itemCreate)
    implementation(projects.pass.features.itemDetails)
    implementation(projects.pass.features.migrate)
    implementation(projects.pass.features.onboarding)
    implementation(projects.pass.features.passkeys)
    implementation(projects.pass.features.profile)
    implementation(projects.pass.features.searchOptions)
    implementation(projects.pass.features.aliasContacts)
    implementation(projects.pass.features.password)
    implementation(projects.pass.features.selectItem)
    implementation(projects.pass.features.settings)
    implementation(projects.pass.features.sharing)
    implementation(projects.pass.features.sync)
    implementation(projects.pass.features.trash)
    implementation(projects.pass.features.vault)
    implementation(projects.pass.features.itemHistory)
    implementation(projects.pass.features.itemOptions)
    implementation(projects.pass.features.inAppMessages)
    implementation(projects.pass.features.secureLinks)
    implementation(projects.pass.features.securityCenter)
    implementation(projects.pass.features.slSync)
    implementation(projects.pass.features.report)
    implementation(projects.pass.features.upsell)

    testImplementation(libs.androidx.compose.ui)
    testImplementation(libs.androidx.compose.uiTooling)
    testImplementation(libs.core.presentation.compose)
    testImplementation(libs.kotlin.reflect)

    testImplementation(libs.showkase)
    kspTest(libs.showkaseProcessor)

    testImplementation(libs.testParameterInjector)
}

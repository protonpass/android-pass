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
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-android")
    alias(libs.plugins.gradlePlugin.compose.compiler)
}

val appVersionName = "1.39.2"
val appVersionCode = versionCode(appVersionName)
val archivesBaseName = "AutofillTestApp-$appVersionName"

fun versionCode(versionName: String): Int {
    val segment = versionName.split('.').map { it.toInt() }
    return (segment[0] * 10000000) + (segment[1] * 100000) + (segment[2] * 1000)
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    namespace = "proton.android.pass.autofill.sample"

    defaultConfig {
        applicationId = "proton.android.pass.autofill.sample"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = appVersionCode
        versionName = appVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
        getByName("test").java.srcDirs("src/test/kotlin")
        getByName("androidTest").java.srcDirs("src/androidTest/kotlin")
    }

    signingConfigs {
        create("release") {
            storeFile = file("keystore/testapp.keystore")
            storePassword = "passtestapp"
            keyAlias = "passtestapp"
            keyPassword = "passtestapp"
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            enableUnitTestCoverage = false
            enableAndroidTestCoverage = false

            signingConfig = signingConfigs.getByName("release")
        }
        release {
            isMinifyEnabled = true
            //isShrinkResources = true // should be replaced by useResourceShrinker
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )

            signingConfig = signingConfigs.getByName("release")
        }
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }

    packaging {
        resources.excludes.add("META-INF/licenses/**")
        resources.excludes.add("META-INF/AL2.0")
        resources.excludes.add("META-INF/LGPL2.1")
    }
}

dependencies {
    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.annotation)
    implementation(libs.material)
    implementation(projects.pass.commonUi.api)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.core.compose)

    implementation(projects.pass.commonUi.api)
    implementation(libs.androidx.credentials)
    implementation(libs.core.presentation.compose)
}

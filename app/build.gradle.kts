/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton Technologies AG and Proton Mail.
 *
 * Proton Mail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Mail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Mail. If not, see <https://www.gnu.org/licenses/>.
 */

import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin")
    id("kotlin-parcelize")
    id("io.sentry.android.gradle")
}

val privateProperties = Properties().apply {
    try {
        load(FileInputStream("private.properties"))
    } catch (exception: java.io.FileNotFoundException) {
        // Provide empty properties to allow the app to be built without secrets
        Properties()
    }
}

val sentryDSN: String? = privateProperties.getProperty("SENTRY_DSN")
val proxyToken: String? = privateProperties.getProperty("PROXY_TOKEN")

val jobId: Int = System.getenv("CI_JOB_ID")?.take(3)?.toInt() ?: 0
val appVersionName: String = "0.2.1"
val appVersionCode: Int = versionCode(appVersionName)
val archivesBaseName = "ProtonPass-$appVersionName"

fun versionCode(versionName: String): Int {
    val segment = versionName.split('.').map { it.toInt() }
    return (segment[0] * 10000000) + (segment[1] * 100000) + (segment[2] * 1000) + jobId
}

base {
    archivesName.set(archivesBaseName)
}

tasks.register("getArchivesName") {
    doLast {
        println("[ARCHIVES_NAME]${archivesBaseName}")
    }
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    namespace = "proton.android.pass"

    defaultConfig {
        applicationId = "proton.android.pass"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = appVersionCode
        versionName = appVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "SENTRY_DSN", sentryDSN.toBuildConfigValue())
        buildConfigField("String", "PROXY_TOKEN", proxyToken.toBuildConfigValue())
        buildConfigField(
            "String",
            "HUMAN_VERIFICATION_HOST",
            "verify.proton.me".toBuildConfigValue()
        )
    }

    buildFeatures {
        buildConfig = true
        compose = true
        dataBinding = true // required by Core presentation
    }

    signingConfigs {
        register("signingKeystore") {
            storeFile = file("$rootDir/keystore/ProtonMail.keystore")
            storePassword = "${privateProperties["keyStorePassword"]}"
            keyAlias = "ProtonMail"
            keyPassword = "${privateProperties["keyStoreKeyPassword"]}"
        }
        register("uploadKeystore") {
            storeFile = file("$rootDir/keystore/upload-keystore")
            storePassword = "${privateProperties["uploadStorePassword"]}"
            keyAlias = "upload"
            keyPassword = "${privateProperties["uploadStorePassword"]}"
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            isTestCoverageEnabled = false
            postprocessing {
                isObfuscate = false
                isOptimizeCode = false
                isRemoveUnusedCode = false
                isRemoveUnusedResources = false
            }
        }
        release {
            isDebuggable = false
            isTestCoverageEnabled = false
            postprocessing {
                isObfuscate = false
                isOptimizeCode = true
                isRemoveUnusedCode = true
                isRemoveUnusedResources = true
                file("proguard").listFiles()?.forEach { proguardFile(it) }
            }
        }
    }

    flavorDimensions += "default"
    productFlavors {
        create("dev") {
            isDefault = true
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            buildConfigField("Boolean", "USE_DEFAULT_PINS", "false")
            buildConfigField("String", "HOST", "\"proton.black\"")
            buildConfigField("String", "HUMAN_VERIFICATION_HOST", "\"verify.proton.black\"")
            buildConfigField("Boolean", "ALLOW_SCREENSHOTS", "true")
            signingConfig = signingConfigs["signingKeystore"]
        }
        create("alpha") {
            applicationIdSuffix = ".alpha"
            versionNameSuffix = "-alpha.$appVersionCode"
            buildConfigField("Boolean", "USE_DEFAULT_PINS", "true")
            buildConfigField("String", "HOST", "\"protonmail.ch\"")
            buildConfigField("Boolean", "ALLOW_SCREENSHOTS", "true")
            signingConfig = signingConfigs["signingKeystore"]
        }
        create("prod") {
            buildConfigField("Boolean", "USE_DEFAULT_PINS", "true")
            buildConfigField("String", "HOST", "\"protonmail.ch\"")
            buildConfigField("Boolean", "ALLOW_SCREENSHOTS", "true")
            signingConfig = signingConfigs["uploadKeystore"]
        }
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
        getByName("test").java.srcDirs("src/test/kotlin")
        getByName("androidTest").java.srcDirs("src/androidTest/kotlin", "src/uiTest/kotlin")
        getByName("androidTest").assets.srcDirs("src/uiTest/assets")
        getByName("dev").res.srcDirs("src/dev/res")
        getByName("alpha").res.srcDirs("src/alpha/res")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
    }

    hilt {
        enableAggregatingTask = true
    }

    packagingOptions {
        resources.excludes.add("META-INF/licenses/**")
        resources.excludes.add("META-INF/AL2.0")
        resources.excludes.add("META-INF/LGPL2.1")
    }

    kapt {
        correctErrorTypes = true
    }
}

dependencies {
    implementation(files("../../proton-libs/gopenpgp/gopenpgp.aar"))
    implementation(libs.accompanist.navigation.animation)
    implementation(libs.accompanist.insets)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.compose.foundationLayout)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundationLayout)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.uiTooling)
    implementation(libs.androidx.startup.runtime)
    implementation(libs.core.account)
    implementation(libs.core.accountManager)
    implementation(libs.core.accountManager.presentation.compose)
    implementation(libs.core.auth)
    implementation(libs.core.challenge)
    implementation(libs.core.country)
    implementation(libs.core.crypto)
    implementation(libs.core.cryptoValidator)
    implementation(libs.core.data)
    implementation(libs.core.dataRoom)
    implementation(libs.core.domain)
    implementation(libs.core.eventManager)
    implementation(libs.core.featureFlag)
    implementation(libs.core.humanVerification)
    implementation(libs.core.key)
    implementation(libs.core.payment)
    implementation(libs.core.plan)
    implementation(libs.core.presentation)
    implementation(libs.core.presentation.compose)
    implementation(libs.core.report)
    implementation(libs.core.user)
    implementation(libs.core.userSettings)
    implementation(libs.core.utilAndroidDagger)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.material)
    implementation(libs.okhttp)
    implementation(libs.plumber)

    debugImplementation(libs.showkase)
    kspDebug(libs.showkaseProcessor)

    implementation(projects.pass.appConfig.api)
    implementation(projects.pass.autofill.impl)
    implementation(projects.pass.biometry.api)
    implementation(projects.pass.biometry.impl)
    implementation(projects.pass.clipboard.impl)
    implementation(projects.pass.crypto.impl)
    implementation(projects.pass.common.api)
    implementation(projects.pass.commonUi.api)
    implementation(projects.pass.composeComponents.impl)
    implementation(projects.pass.data.api)
    implementation(projects.pass.data.impl)
    implementation(projects.pass.domain)
    implementation(projects.pass.featureAuth.impl)
    implementation(projects.pass.featureCreateItem.impl)
    implementation(projects.pass.log.api)
    implementation(projects.pass.log.impl)
    implementation(projects.pass.navigation.api)
    implementation(projects.pass.network.api)
    implementation(projects.pass.network.impl)
    implementation(projects.pass.notifications.api)
    implementation(projects.pass.notifications.impl)
    implementation(projects.pass.preferences.api)
    implementation(projects.pass.preferences.impl)
    implementation(projects.pass.presentation)
    implementation(projects.pass.tracing.impl)

    debugImplementation(libs.leakCanary)
    debugImplementation(libs.androidx.compose.uiTooling)

    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.android.compiler)
    kapt(libs.androidx.hilt.compiler)
}

fun String?.toBuildConfigValue() = if (this != null) "\"$this\"" else "null"

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

import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin")
    id("kotlin-parcelize")
    id("io.sentry.android.gradle")
    id("org.jetbrains.kotlin.kapt")
}

val privateProperties = Properties().apply {
    try {
        load(rootDir.resolve("private.properties").inputStream())
    } catch (exception: java.io.FileNotFoundException) {
        // Provide empty properties to allow the app to be built without secrets
        Properties()
    }
}

val sentryDSN: String? = privateProperties.getProperty("SENTRY_DSN")
val proxyToken: String? = privateProperties.getProperty("PROXY_TOKEN")
val testEnvUrl: String = System.getenv("TEST_ENV_URL") ?: "api.proton.black"
val prodEnvUrl: String = System.getenv("PROD_ENV_URL") ?: "pass-api.proton.me"
val prodHvUrl: String = if (System.getenv("PROD_ENV_URL").isNullOrBlank()) "verify.proton.me" else "verify.proton.black"
val isCustomBuild: Boolean = !System.getenv("PROD_ENV_URL").isNullOrBlank()
val isApkBuild: Boolean = project.findProperty("apkBuild") == "true"

println("""
    ------- BUILD INFO -------
    testEnvUrl: $testEnvUrl
    prodEnvUrl: $prodEnvUrl
    prodHvUrl: $prodHvUrl
    isCustomBuild: $isCustomBuild
    isApkBuild: $isApkBuild
    --------------------------
""".trimIndent())

val jobId: Int = System.getenv("CI_JOB_ID")?.take(3)?.toInt() ?: 0
val appVersionName: String = "1.6.3"
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
        testInstrumentationRunner = "me.proton.core.test.android.ProtonHiltTestRunner"

        buildConfigField("String", "SENTRY_DSN", sentryDSN.toBuildConfigValue())
        buildConfigField("String", "PROXY_TOKEN", proxyToken.toBuildConfigValue())
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
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

    flavorDimensions += "version"
    productFlavors {
        create("dev") {
            dimension = "version"
            isDefault = true
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            buildConfigField("Boolean", "ALLOW_SCREENSHOTS", "true")
            signingConfig = signingConfigs["signingKeystore"]
        }
        create("alpha") {
            dimension = "version"
            applicationIdSuffix = ".alpha"
            versionNameSuffix = "-alpha.$appVersionCode"
            buildConfigField("Boolean", "ALLOW_SCREENSHOTS", "true")
            signingConfig = signingConfigs["signingKeystore"]
        }
        create("play") {
            dimension = "version"
            buildConfigField("Boolean", "ALLOW_SCREENSHOTS", "true")
            signingConfig = if (isCustomBuild || isApkBuild) {
                println("Using signing keystore")
                signingConfigs["signingKeystore"]
            } else {
                println("Using upload keystore")
                signingConfigs["uploadKeystore"]
            }
        }
    }
    flavorDimensions += "env"
    productFlavors {
        create("black") {
            dimension = "env"
            applicationIdSuffix = ".black"
            buildConfigField("Boolean", "USE_DEFAULT_PINS", "false")
            buildConfigField("String", "HOST", testEnvUrl.toBuildConfigValue())
            buildConfigField("String", "HV_HOST", "verify.proton.black".toBuildConfigValue())
        }
        create("prod") {
            dimension = "env"
            buildConfigField("Boolean", "USE_DEFAULT_PINS", isCustomBuild.toString())
            buildConfigField("String", "HOST", prodEnvUrl.toBuildConfigValue())
            buildConfigField("String", "HV_HOST", prodHvUrl.toBuildConfigValue())
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
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

    testOptions {
        managedDevices {
            devices {
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
    implementation(files("../../proton-libs/gopenpgp/gopenpgp.aar"))
    implementation(libs.accompanist.navigation.animation)
    implementation(libs.accompanist.navigation.material)
    implementation(libs.accompanist.insets)
    implementation(libs.accompanist.systemUiController)
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
    implementation(libs.androidx.profileinstaller)
    implementation(libs.androidx.startup.runtime)
    implementation(libs.coil)
    implementation(libs.core.account)
    implementation(libs.core.accountManager)
    implementation(libs.core.accountManager.presentation.compose)
    implementation(libs.core.accountRecovery)
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
    implementation(libs.core.keyTransparency)
    implementation(libs.core.network)
    implementation(libs.core.notification)
    implementation(libs.core.observability)
    implementation(libs.core.payment)
    implementation(libs.core.paymentIap)
    implementation(libs.core.plan)
    implementation(libs.core.presentation)
    implementation(libs.core.presentation.compose)
    implementation(libs.core.push)
    implementation(libs.core.report)
    implementation(libs.core.user)
    implementation(libs.core.userSettings)
    implementation(libs.core.utilAndroidDagger)
    implementation(libs.kotlinx.collections)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.material)
    implementation(libs.okhttp)
    implementation(libs.plumber)
    implementation(libs.kotlinx.collections)
    implementation(libs.kotlinx.datetime)

    debugImplementation(libs.showkase)
    kspDebug(libs.showkaseProcessor)

    implementation(projects.pass.account.impl)
    implementation(projects.pass.appConfig.api)
    implementation(projects.pass.autofill.impl)
    implementation(projects.pass.biometry.api)
    implementation(projects.pass.biometry.impl)
    implementation(projects.pass.clipboard.impl)
    implementation(projects.pass.common.api)
    implementation(projects.pass.common.impl)
    implementation(projects.pass.commonUi.api)
    implementation(projects.pass.commonUi.impl)
    implementation(projects.pass.commonUiModels.api)
    implementation(projects.pass.composeComponents.impl)
    implementation(projects.pass.crypto.impl)
    implementation(projects.pass.data.api)
    implementation(projects.pass.data.impl)
    implementation(projects.pass.domain)
    implementation(projects.pass.image.api)
    implementation(projects.pass.image.impl)
    implementation(projects.pass.inAppUpdates.api)
    implementation(projects.pass.inAppUpdates.impl)
    implementation(projects.pass.inAppReview.api)
    implementation(projects.pass.inAppReview.impl)
    implementation(projects.pass.featureAccount.impl)
    implementation(projects.pass.featureAuth.impl)
    implementation(projects.pass.featureFeatureFlags.impl)
    implementation(projects.pass.featureHome.impl)
    implementation(projects.pass.featureItemCreate.impl)
    implementation(projects.pass.featureItemDetail.impl)
    implementation(projects.pass.featureMigrate.impl)
    implementation(projects.pass.featureOnboarding.impl)
    implementation(projects.pass.featurePassword.impl)
    implementation(projects.pass.featureProfile.impl)
    implementation(projects.pass.featureSettings.impl)
    implementation(projects.pass.featureSearchOptions.api)
    implementation(projects.pass.featureSearchOptions.impl)
    implementation(projects.pass.featureTrial.impl)
    implementation(projects.pass.featureVault.impl)
    implementation(projects.pass.log.api)
    implementation(projects.pass.log.impl)
    implementation(projects.pass.navigation.api)
    implementation(projects.pass.network.api)
    implementation(projects.pass.network.impl)
    implementation(projects.pass.notifications.api)
    implementation(projects.pass.notifications.impl)
    implementation(projects.pass.preferences.api)
    implementation(projects.pass.preferences.impl)
    implementation(projects.pass.telemetry.impl)
    implementation(projects.pass.totp.impl)
    implementation(projects.pass.tracing.impl)

    debugImplementation(libs.leakCanary)
    debugImplementation(libs.androidx.compose.uiTooling)

    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.android.compiler)
    kapt(libs.androidx.hilt.compiler)

    kaptAndroidTest(libs.dagger.hilt.android.compiler)
    androidTestImplementation(libs.bundles.test.android) {
        exclude(module = "protobuf-lite")
    }
    androidTestImplementation(libs.bundles.core.test) {
        exclude(module = "protobuf-lite")
    }
    androidTestUtil(libs.androidx.test.orchestrator)
}

fun String?.toBuildConfigValue() = if (this != null) "\"$this\"" else "null"

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

import com.android.build.api.dsl.VariantDimension
import configuration.EnvironmentConfigSettings
import configuration.extensions.protonEnvironment
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin")
    id("kotlin-parcelize")
    id("io.sentry.android.gradle")
    id("org.jetbrains.kotlin.kapt")
    id("androidx.baselineprofile")
    alias(libs.plugins.gradlePlugin.proton.environmentConfig)
    alias(libs.plugins.gradlePlugin.dependency.guard)
    alias(libs.plugins.gradlePlugin.compose.compiler)
    alias(libs.plugins.gradlePlugin.play)
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
val accountSentryDSN: String? = System.getenv("ACCOUNT_SENTRY_DSN")
val atlasProxyToken: String? = privateProperties.getProperty("PROXY_TOKEN")
val customEnvUrl: String? = System.getenv("PROD_ENV_URL")
val isCustomBuild: Boolean = !System.getenv("PROD_ENV_URL").isNullOrBlank()
val isApkBuild: Boolean = project.findProperty("apkBuild") == "true"

println(
    """
    ------- BUILD INFO -------
    customEnvUrl: $customEnvUrl
    isCustomBuild: $isCustomBuild
    isApkBuild: $isApkBuild
    --------------------------
""".trimIndent()
)

val jobId: Int = System.getenv("CI_JOB_ID")?.take(3)?.toInt() ?: 0
val appVersionName: String = "1.35.0"
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
        buildConfigField("String", "ACCOUNT_SENTRY_DSN", accountSentryDSN.toBuildConfigValue())

        setAssetLinksResValue("proton.me")

        protonEnvironment {
            proxyToken = atlasProxyToken
            apiPrefix = "pass-api"
        }

        ndk {
            abiFilters += "armeabi-v7a"
            abiFilters += "arm64-v8a"
            abiFilters += "x86_64"
        }
    }

    androidResources {
        generateLocaleConfig = true
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }

    buildFeatures {
        buildConfig = true
        compose = true
        dataBinding = true // required by Core presentation
    }

    buildTypes {
        debug {
            isDebuggable = true
            enableUnitTestCoverage = false
            enableAndroidTestCoverage = false
            postprocessing {
                isObfuscate = false
                isOptimizeCode = false
                isRemoveUnusedCode = false
                isRemoveUnusedResources = false
            }
        }
        release {
            isDebuggable = false
            enableUnitTestCoverage = false
            enableAndroidTestCoverage = false
            postprocessing {
                isObfuscate = false
                isOptimizeCode = true
                isRemoveUnusedCode = true
                isRemoveUnusedResources = true
                file("proguard").listFiles()?.forEach { proguardFile(it) }
            }
            signingConfig = signingConfigs.getByName("debug")
        }
        create("benchmarkRelease") {
            signingConfig = signingConfigs.getByName("debug")
        }
        create("nonMinifiedRelease") {
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    flavorDimensions += "version"
    productFlavors {
        create("dev") {
            dimension = "version"
            isDefault = true
            resourceConfigurations.addAll(listOf("en", "xxhdpi"))
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            buildConfigField("Boolean", "ALLOW_SCREENSHOTS_DEFAULT_VALUE", "true")
        }
        create("alpha") {
            dimension = "version"
            applicationIdSuffix = ".alpha"
            versionNameSuffix = "-alpha.$appVersionCode"
            buildConfigField("Boolean", "ALLOW_SCREENSHOTS_DEFAULT_VALUE", "false")
        }
        create("play") {
            dimension = "version"
            buildConfigField("Boolean", "ALLOW_SCREENSHOTS_DEFAULT_VALUE", "false")
        }
        create("fdroid") {
            dimension = "version"
            applicationIdSuffix = ".fdroid"
            buildConfigField("Boolean", "ALLOW_SCREENSHOTS_DEFAULT_VALUE", "false")
        }
        create("quest") {
            dimension = "version"
            applicationIdSuffix = ".quest"
            buildConfigField("Boolean", "ALLOW_SCREENSHOTS_DEFAULT_VALUE", "false")
        }
    }
    flavorDimensions += "env"
    productFlavors {
        create("black") {
            dimension = "env"
            applicationIdSuffix = ".black"

            val protonHost = customEnvUrl ?: "proton.black"
            protonEnvironment {
                host = protonHost

                printInfo(name)
            }

            setAssetLinksResValue(protonHost)
        }
        create("prod") {
            dimension = "env"
            protonEnvironment {
                host = customEnvUrl ?: host
                // If we are creating a custom build (prod build that points to scientist env)
                // do not use the default pins
                useDefaultPins = !isCustomBuild
                apiPrefix = customEnvUrl?.let { "api" } ?: "pass-api"

                printInfo(name)
            }
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
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    hilt {
        enableAggregatingTask = true
    }

    packaging {
        resources.excludes.add("META-INF/licenses/**")
        resources.excludes.add("META-INF/AL2.0")
        resources.excludes.add("META-INF/LGPL2.1")
        resources.excludes.add("META-INF/LICENSE.md")
        resources.excludes.add("META-INF/LICENSE-notice.md")
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

configurations {
    // Remove duplicate classes (keep "org.jetbrains").
    implementation.get().exclude(mapOf("group" to "com.intellij", "module" to "annotations"))
    implementation.get().exclude(mapOf("group" to "org.intellij", "module" to "annotations"))
}

fun DependencyHandlerScope.addSpecialLib(
    default: Any,
    overrides: Map<String, Any?> = emptyMap()
) {
    val variants = listOf("dev", "alpha", "play", "quest", "fdroid")

    variants.forEach { variant ->
        val dep = overrides[variant].let { override ->
            if (override != null || variant in overrides) override else default
        }

        dep?.let {
            configurations.getByName("${variant}Implementation")(it)
        }
    }
}

fun DependencyHandlerScope.addDevBlackImplementation(
    default: Any,
    devBlack: Any,
) {
    val devBlackImplementation = configurations.maybeCreate("devBlackImplementation")
    val devProdImplementation = configurations.maybeCreate("devProdImplementation")
    val alphaImplementation = configurations.getByName("alphaImplementation")
    val playImplementation = configurations.getByName("playImplementation")
    val fdroidImplementation = configurations.getByName("fdroidImplementation")
    val questImplementation = configurations.getByName("questImplementation")

    devBlackImplementation(devBlack)
    devProdImplementation(default)
    alphaImplementation(default)
    playImplementation(default)
    fdroidImplementation(default)
    questImplementation(default)
}

dependencies {
    coreLibraryDesugaring(libs.android.desugar)
    implementation(files("../../proton-libs/gopenpgp/gopenpgp.aar"))
    implementation(libs.accompanist.navigation.material)
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
    implementation(libs.core.authFidoDagger)
    implementation(libs.core.authFidoDomain)
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
    implementation(libs.core.userRecovery)

    addSpecialLib(
        default = libs.core.payment,
        overrides = mapOf("fdroid" to null, "quest" to null)
    )

    addSpecialLib(
        default = libs.core.paymentIap,
        overrides = mapOf("fdroid" to null, "quest" to null)
    )
    addSpecialLib(
        default = libs.core.authFidoPlay,
        overrides = mapOf("fdroid" to null, "quest" to null)
    )

    implementation(libs.core.plan)
    implementation(libs.core.presentation)
    implementation(libs.core.presentation.compose)
    implementation(libs.core.push)
    implementation(libs.core.report)
    implementation(libs.core.telemetry.data)
    implementation(libs.core.telemetry.domain)
    implementation(libs.core.telemetry.presentation)
    implementation(libs.core.user)
    implementation(libs.core.userSettings)
    implementation(libs.core.utilAndroidDagger)
    implementation(libs.core.config.data)
    implementation(libs.core.biometric)
    implementation(libs.core.deviceMigration)
    implementation(libs.core.passValidator)
    addDevBlackImplementation(
        default = libs.core.config.dagger.staticDefaults,
        devBlack = libs.core.config.dagger.contentProvider
    )
    implementation(libs.kotlinx.collections)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.material)
    implementation(libs.pass.common)
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
    implementation(projects.pass.commonPresentation.api)
    implementation(projects.pass.commonPresentation.impl)
    implementation(projects.pass.commonRust.api)
    implementation(projects.pass.commonRust.impl)
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
    addSpecialLib(
        default = projects.pass.inAppUpdates.impl,
        overrides = mapOf(
            "fdroid" to projects.pass.inAppUpdates.noOp,
            "quest" to projects.pass.inAppUpdates.noOp
        )
    )

    implementation(projects.pass.inAppReview.api)
    addSpecialLib(
        default = projects.pass.inAppReview.impl,
        overrides = mapOf(
            "fdroid" to projects.pass.inAppReview.noOp,
            "quest" to projects.pass.inAppReview.noOp
        )
    )

    implementation(projects.pass.features.account)
    implementation(projects.pass.features.aliasContacts)
    implementation(projects.pass.features.auth)
    implementation(projects.pass.features.credentials)
    implementation(projects.pass.features.featureFlags)
    implementation(projects.pass.features.home)
    implementation(projects.pass.features.itemCreate)
    implementation(projects.pass.features.migrate)
    implementation(projects.pass.features.onboarding)
    implementation(projects.pass.features.passkeys)
    implementation(projects.pass.features.profile)
    implementation(projects.pass.features.settings)
    implementation(projects.pass.searchOptions.api)
    implementation(projects.pass.features.attachments)
    implementation(projects.pass.features.searchOptions)
    implementation(projects.pass.features.password)
    implementation(projects.pass.features.selectItem)
    implementation(projects.pass.features.sharing)
    implementation(projects.pass.features.sync)
    implementation(projects.pass.features.trial)
    implementation(projects.pass.features.vault)
    implementation(projects.pass.features.accountSelector)
    implementation(projects.pass.features.extrapassword)
    implementation(projects.pass.features.itemDetails)
    implementation(projects.pass.features.itemHistory)
    implementation(projects.pass.features.itemOptions)
    implementation(projects.pass.features.itemTrash)
    implementation(projects.pass.features.secureLinks)
    implementation(projects.pass.features.securityCenter)
    implementation(projects.pass.features.report)
    implementation(projects.pass.features.slSync)
    implementation(projects.pass.features.upsell)
    implementation(projects.pass.features.inAppMessages)
    implementation(projects.pass.log.api)
    implementation(projects.pass.log.impl)
    implementation(projects.pass.navigation.api)
    implementation(projects.pass.network.api)
    implementation(projects.pass.network.impl)
    implementation(projects.pass.notifications.api)
    implementation(projects.pass.notifications.impl)
    implementation(projects.pass.passkeys.api)
    implementation(projects.pass.passkeys.impl)
    implementation(projects.pass.preferences.api)
    implementation(projects.pass.preferences.impl)
    implementation(projects.pass.securityCenter.api)
    implementation(projects.pass.securityCenter.impl)
    implementation(projects.pass.telemetry.impl)
    implementation(projects.pass.totp.impl)
    implementation(projects.pass.files.api)
    implementation(projects.pass.files.impl)

    addSpecialLib(
        default = projects.pass.tracing.impl,
        overrides = mapOf("fdroid" to projects.pass.tracing.noOp)
    )

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
    androidTestImplementation(libs.core.test.android.test.rule)
    androidTestUtil(libs.androidx.test.orchestrator)
    baselineProfile(projects.appmacrobenchmark)

    "questImplementation"(libs.meta.horizon.sdk)
    "questImplementation"(libs.meta.horizon.billing)
}

fun String?.toBuildConfigValue() = if (this != null) "\"$this\"" else "null"

fun EnvironmentConfigSettings.printInfo(name: String) {
    println(
        """
            - ENV INFO ($name) -
            apiHost: $apiHost
            hv3Host: $hv3Host
            --------------------------
        """.trimIndent()
    )
}

sentry {
    autoInstallation.enabled.set(false)
    ignoredBuildTypes.set(setOf("debug"))
    ignoredFlavors.set(setOf("fdroid"))
}

fun VariantDimension.setAssetLinksResValue(host: String) {
    resValue(
        type = "string", name = "asset_statements",
        value = """
            [{
              "relation": ["delegate_permission/common.handle_all_urls", "delegate_permission/common.get_login_creds"],
              "target": { "namespace": "web", "site": "https://$host" }
            }]
        """.trimIndent()
    )
}

dependencyGuard {
    configuration("playProdReleaseRuntimeClasspath") {
        artifacts = true
        modules = false

        allowedFilter = {
            !it.contains("junit")
        }
    }
    configuration("fdroidProdReleaseRuntimeClasspath") {
        artifacts = true
        modules = false

        allowedFilter = {
            !it.contains("junit")
            !it.contains("com.android.billingclient")
            !it.contains("com.google.android.gms")
            !it.contains("com.google.android.play")
            !it.contains("io.sentry")
        }
    }
    configuration("questProdReleaseRuntimeClasspath") {
        artifacts = true
        modules = false

        allowedFilter = {
            !it.contains("junit")
            !it.contains("com.android.billingclient")
            !it.contains("com.google.android.gms")
            !it.contains("com.google.android.play")
        }
    }
}

baselineProfile {
    saveInSrc = true
    dexLayoutOptimization = true
}

play {
    serviceAccountCredentials.set(file("/tmp/play-service-account.json"))
    track.set("internal")
    releaseStatus.set(com.github.triplet.gradle.androidpublisher.ReleaseStatus.DRAFT)
    artifactDir.set(file("$rootDir/signedArtifacts"))
}

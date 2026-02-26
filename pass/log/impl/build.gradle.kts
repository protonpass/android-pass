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
    id("dagger.hilt.android.plugin")
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "proton.android.pass.log.impl"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        
    }

    flavorDimensions += "version"
    productFlavors {
        maybeCreate("dev")
        maybeCreate("alpha")
        maybeCreate("play")
        maybeCreate("fdroid")
        maybeCreate("quest")
        maybeCreate("nogms")
    }
}

androidComponents.beforeVariants { variant ->
    variant.enableAndroidTest = false
}

fun DependencyHandlerScope.addSpecialLib(
    default: Any,
    overrides: Map<String, Any?> = emptyMap()
) {
    val variants = listOf("dev", "alpha", "play", "quest", "fdroid", "nogms")
    variants.forEach { variant ->
        val dep = overrides[variant].let { override ->
            if (override != null || variant in overrides) override else default
        }

        dep?.let {
            configurations.getByName("${variant}Implementation")(it)
        }
    }
}

dependencies {
    api(projects.pass.log.api)

    implementation(projects.pass.common.api)
    implementation(projects.pass.commonUi.api)
    implementation(libs.core.accountManager.domain)

    addSpecialLib(
        default = projects.pass.tracing.impl,
        overrides = mapOf(
            "fdroid" to projects.pass.tracing.noOp
        )
    )

    implementation(projects.pass.appConfig.api)

    implementation(libs.androidx.startup.runtime)
    implementation(libs.timber)
    implementation(libs.core.utilKotlin)
    implementation(libs.kotlinx.datetime)

    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.android.compiler)
    kapt(libs.androidx.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(projects.pass.common.fakes)
    testImplementation(projects.pass.account.fakes)
}

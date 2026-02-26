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
    id("com.google.protobuf")
    id("org.jetbrains.kotlin.kapt")
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    namespace = "proton.android.pass.preferences.implementation"

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        
    }
    sourceSets {
        getByName("debug") {
            java.srcDirs("build/generated/source/proto/debug")
        }
        getByName("release") {
            java.srcDirs("build/generated/source/proto/release")
        }
    }
}

androidComponents.beforeVariants { variant ->
    variant.enableAndroidTest = false
}

protobuf {
    protoc {
        artifact = project.libs.google.protobuf.protoc.get().toString()
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                val java by registering {
                    option("lite")
                }
            }
        }
        ofBuildType("release")
    }
}

dependencies {
    api(projects.pass.preferences.api)

    implementation(libs.androidx.datastore)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.startup.runtime)
    implementation(libs.core.accountManager.domain)
    implementation(libs.core.featureFlag.domain)
    implementation(libs.core.utilKotlin)
    implementation(libs.google.protobuf.kotlin.lite)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime)

    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.android.compiler)
    kapt(libs.androidx.hilt.compiler)

    implementation(projects.pass.appConfig.api)
    implementation(projects.pass.common.api)
    implementation(projects.pass.commonUi.api)
    implementation(projects.pass.domain)
    implementation(projects.pass.data.api)
    implementation(projects.pass.log.api)
}

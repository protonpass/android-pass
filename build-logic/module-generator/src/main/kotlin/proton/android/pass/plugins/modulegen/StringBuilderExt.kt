/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.plugins.modulegen

fun StringBuilder.appendJvmPlugin(): StringBuilder = append(
    """
        plugins {
            id("org.jetbrains.kotlin.jvm")
        }   
    """.trimIndent()
)

fun StringBuilder.appendAndroidLibraryPlugin(namespace: String): StringBuilder = append(
    """
        plugins {
            id("com.android.library")
            id("org.jetbrains.kotlin.android")
        }
        
        android {
            namespace = "$namespace"
            compileSdk = libs.versions.compileSdk.get().toInt()
            
            defaultConfig {
                minSdk = libs.versions.minSdk.get().toInt()
                targetSdk = libs.versions.targetSdk.get().toInt()
            }
            
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
        
            kotlinOptions {
                jvmTarget = JavaVersion.VERSION_17.toString()
            }
        }
        
        androidComponents.beforeVariants { variant ->
            variant.enableAndroidTest = false
        }
    """.trimIndent()
)

fun StringBuilder.appendLibraryDependency(projectAccessor: String): StringBuilder = append(
    """
        dependencies {
            api(projects.$projectAccessor.api)
        }
    """.trimIndent()
)

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

import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    alias(libs.plugins.gradlePlugin.proton.detekt)
    alias(libs.plugins.gradlePlugin.versions)
    alias(libs.plugins.gradlePlugin.dependency.analysis)
    alias(libs.plugins.gradlePlugin.doctor)
    alias(libs.plugins.gradlePlugin.application) apply false
    alias(libs.plugins.gradlePlugin.library) apply false
    alias(libs.plugins.gradlePlugin.ksp) apply false
    alias(libs.plugins.gradlePlugin.kotlin.jvm) apply false
    alias(libs.plugins.gradlePlugin.kotlin.serialization) apply false
    alias(libs.plugins.gradlePlugin.hilt) apply false
    alias(libs.plugins.gradlePlugin.paparazzi) apply false
    alias(libs.plugins.gradlePlugin.protobuf) apply false
    alias(libs.plugins.gradlePlugin.sentry) apply false
}

val isCI = System.getenv().containsKey("CI")

doctor {
    javaHome {
        ensureJavaHomeIsSet.set(!isCI)
        ensureJavaHomeMatches.set(!isCI)
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

setupDependenciesPlugin()

kotlinCompilerArgs(
    "-opt-in=kotlin.RequiresOptIn",
    "-opt-in=androidx.compose.ui.ExperimentalComposeUiApi",
    "-opt-in=androidx.lifecycle.compose.ExperimentalLifecycleComposeApi",
    // Enables experimental Coroutines (runBlockingTest).
    "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
    // Enables experimental Time (Turbine).
    "-opt-in=kotlin.time.ExperimentalTime"
)

fun Project.kotlinCompilerArgs(vararg extraCompilerArgs: String) {
    for (sub in subprojects) {
        sub.tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
            kotlinOptions { freeCompilerArgs = freeCompilerArgs + extraCompilerArgs }
        }
    }
}

fun Project.setupDependenciesPlugin() {
    // https://github.com/ben-manes/gradle-versions-plugin
    tasks.withType<DependencyUpdatesTask> {
        rejectVersionIf {
            isNonStable(candidate.version) && !isNonStable(currentVersion)
        }
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

protonDetekt {
    threshold = 0
}

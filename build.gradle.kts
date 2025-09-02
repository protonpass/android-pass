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

plugins {
    alias(libs.plugins.gradlePlugin.proton.detekt)
    alias(libs.plugins.gradlePlugin.dependency.analysis)
    alias(libs.plugins.gradlePlugin.doctor)
    alias(libs.plugins.gradlePlugin.fulladle)
    alias(libs.plugins.gradlePlugin.android.baselineprofile) apply false
    alias(libs.plugins.gradlePlugin.android.benchmark) apply false
    alias(libs.plugins.gradlePlugin.application) apply false
    alias(libs.plugins.gradlePlugin.compose.compiler) apply false
    alias(libs.plugins.gradlePlugin.dependency.guard) apply false
    alias(libs.plugins.gradlePlugin.library) apply false
    alias(libs.plugins.gradlePlugin.test) apply false
    alias(libs.plugins.gradlePlugin.ksp) apply false
    alias(libs.plugins.gradlePlugin.kotlin.jvm) apply false
    alias(libs.plugins.gradlePlugin.kotlin.serialization) apply false
    alias(libs.plugins.gradlePlugin.hilt) apply false
    alias(libs.plugins.gradlePlugin.paparazzi) apply false
    alias(libs.plugins.gradlePlugin.protobuf) apply false
    alias(libs.plugins.gradlePlugin.sentry) apply false
    alias(libs.plugins.gradlePlugin.test.logger) apply false
    id("proton.android.pass.module-gen")
}

val isCI = System.getenv().containsKey("CI")

subprojects {
    apply {
        plugin("com.adarshr.test-logger")
    }

    tasks.withType<Test> {
        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
    }

    configure<com.adarshr.gradle.testlogger.TestLoggerExtension> {
        theme = com.adarshr.gradle.testlogger.theme.ThemeType.MOCHA_PARALLEL
    }

    // Paparazzi workaround
    plugins.withId("app.cash.paparazzi") {
        // Defer until afterEvaluate so that testImplementation is created by Android plugin.
        afterEvaluate {
            dependencies.constraints {
                add("testImplementation", "com.google.guava:guava") {
                    attributes {
                        attribute(
                            TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE,
                            objects.named(TargetJvmEnvironment::class.java, TargetJvmEnvironment.STANDARD_JVM)
                        )
                    }
                    because("LayoutLib and sdk-common depend on Guava's -jre published variant." +
                            "See https://github.com/cashapp/paparazzi/issues/906.")
                }
            }
        }
    }
}

doctor {
    warnWhenNotUsingParallelGC.set(false)
    javaHome {
        ensureJavaHomeIsSet.set(!isCI)
        ensureJavaHomeMatches.set(!isCI)
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

for (project in subprojects) {
    project.tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + listOf(
                "-opt-in=kotlin.RequiresOptIn",
                // Enables experimental Time (Turbine).
                "-opt-in=kotlin.time.ExperimentalTime"
            )
            val hasCoroutines = project.configurations.findByName("implementation")
                ?.dependencies
                ?.any { it.name.contains("kotlinx-coroutines-core") } == true
            val hasCoroutinesInTest = project.configurations.findByName("testImplementation")
                ?.dependencies
                ?.any { it.name.contains("kotlinx-coroutines-test") } == true
            if (hasCoroutines || hasCoroutinesInTest) {
                freeCompilerArgs =
                    freeCompilerArgs + "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
            }
        }
    }
}

protonDetekt {
    threshold = 0
}

allprojects {
    // Force Java toolchain to 17 for everything
    plugins.withType<JavaPlugin> {
        extensions.configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(17))
            }
        }
    }

    // Force Kotlin jvmTarget = 17 for all subprojects
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "17"
        }
    }
    tasks.withType(org.jetbrains.kotlin.gradle.dsl.KotlinCompile::class.java).configureEach {
        kotlinOptions {
            // Trigger this with:
            // ./gradlew assembleRelease -PenableMultiModuleComposeReports=true --rerun-tasks
            if (project.findProperty("enableMultiModuleComposeReports") == "true") {
                freeCompilerArgs = freeCompilerArgs + listOf(
                    "-P",
                    "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" + rootProject.buildDir.absolutePath + "/compose_metrics/"
                )
                freeCompilerArgs = freeCompilerArgs + listOf(
                    "-P",
                    "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" + rootProject.buildDir.absolutePath + "/compose_metrics/"
                )
            }
        }
    }
}

fladle {
    debugApk.set("$rootDir/app/build/outputs/apk/devBlack/debug/app-dev-black-debug.apk")
    serviceAccountCredentials.set(File("/tmp/service-account.json"))
    devices.set(
        listOf(
            mapOf("model" to "SmallPhone.arm", "version" to "\"27\""),
            mapOf("model" to "Pixel2.arm", "version" to "\"33\""),
        )
    )
    useOrchestrator.set(true)
    flakyTestAttempts.set(1)
}

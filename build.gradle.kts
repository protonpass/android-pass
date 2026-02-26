import com.adarshr.gradle.testlogger.TestLoggerExtension
import com.adarshr.gradle.testlogger.theme.ThemeType
import org.jetbrains.kotlin.gradle.dsl.KotlinTopLevelExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/*
 * Copyright (c) 2022-2026 Proton Technologies AG
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

// Pass Common AAR replacement setup
val passCommonAarPath = project.findProperty("passCommonAarPath")?.toString()
    ?: System.getenv("PASS_COMMON_AAR_PATH")
    ?: "libs/lib-release.aar"
val passCommonAar = File(rootProject.projectDir, passCommonAarPath)

val versionCatalog = extensions.findByType<VersionCatalogsExtension>()?.named("libs")
val passCommon = versionCatalog?.findLibrary("pass-common")?.get()?.get()
val jna = versionCatalog?.findLibrary("jna")?.get()?.get()

subprojects {
    apply {
        plugin("com.adarshr.test-logger")
    }

    tasks.withType<Test> {
        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
    }

    configure<TestLoggerExtension> {
        theme = ThemeType.MOCHA_PARALLEL
    }

    // Pass Common AAR replacement
    afterEvaluate {
        configurations.configureEach {
            withDependencies {
                if (passCommon == null || jna == null) return@withDependencies

                val wasPassCommonRemoved = removeIf { dependency ->
                    dependency.group == passCommon.module.group &&
                            dependency.name == passCommon.module.name
                }

                if (!wasPassCommonRemoved) return@withDependencies

                if (passCommonAar.exists()) {
                    // Use local AAR with JNA dependency
                    add(project.dependencies.create(files(passCommonAar)))
                    add(project.dependencies.create("$jna@aar"))
                    logger.quiet("✅  Using local AAR: ${passCommonAar.name} (${passCommon.module})")
                } else {
                    // Fallback to remote dependency (default behavior, silent)
                    add(passCommon)
                }
            }
        }
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
                            objects.named(
                                TargetJvmEnvironment::class.java,
                                TargetJvmEnvironment.STANDARD_JVM
                            )
                        )
                    }
                    because(
                        "LayoutLib and sdk-common depend on Guava's -jre published variant." +
                                "See https://github.com/cashapp/paparazzi/issues/906."
                    )
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
    project.tasks.withType<KotlinCompile> {
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
    // Force JVM toolchain to 17 for all subprojects
    plugins.withType<KotlinBasePlugin> {
        extensions.configure<KotlinTopLevelExtension> {
            jvmToolchain(17)
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

val filterFlankConfig = tasks.register("filterFlankConfigForSelectiveTests") {
    val apksProp = providers.gradleProperty("selective.test.apks")
    onlyIf { apksProp.isPresent }
    dependsOn("writeConfigProps")
    doLast {
        val builtApks = apksProp.get().split(",").map { it.trim() }.toSet()
        val flankYml = layout.buildDirectory.file("fladle/flank.yml").get().asFile
        if (!flankYml.exists()) return@doLast
        val lines = flankYml.readLines().toMutableList()
        val mainTestIdx = lines.indexOfFirst { line ->
            val t = line.trim()
            t.startsWith("test: /") // not prefixed with '- ', so this is the gcloud test field
        }
        if (mainTestIdx != -1) {
            val mainApk = lines[mainTestIdx].trim().removePrefix("test: ")
            if (mainApk !in builtApks) {
                val replacement = lines
                    .mapNotNull { line ->
                        val t = line.trim()
                        if (t.startsWith("- test: /")) t.removePrefix("- test: ") else null
                    }
                    .firstOrNull { it in builtApks }
                if (replacement != null) {
                    lines[mainTestIdx] = lines[mainTestIdx].replace(mainApk, replacement)
                    lines.removeAll { it.trim() == "- test: $replacement" }
                    logger.lifecycle("Selective filter: swapped main test APK → $replacement")
                } else {
                    logger.warn("Selective filter: main test APK not built and no replacement available")
                }
            }
        }

        val filtered = lines
            .filter { line ->
                val t = line.trim()
                !t.startsWith("- test: /") || t.removePrefix("- test: ") in builtApks
            }
            .joinToString("\n")
        flankYml.writeText("$filtered\n")
        logger.lifecycle("Selective filter: kept ${builtApks.size} APKs in Flank config.")
    }
}

tasks.matching { it.name == "execFlank" }.configureEach {
    dependsOn(filterFlankConfig)
}

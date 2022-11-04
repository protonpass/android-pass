package me.proton.android.pass.plugins.modulegen


fun StringBuilder.appendConfiguration(moduleConfiguration: Configuration): StringBuilder =
    when (moduleConfiguration) {
        Configuration.API -> appendJvmPlugin()
        Configuration.IMPL -> appendAndroidLibraryPlugin()
            .appendLibraryDependency()
        Configuration.FAKES -> appendAndroidLibraryPlugin()
            .appendLibraryDependency()
    }

private fun StringBuilder.appendJvmPlugin(): StringBuilder = append(
    """
    plugins {
        id("org.jetbrains.kotlin.jvm")
    }
    
"""
        .trimIndent()
)

private fun StringBuilder.appendAndroidLibraryPlugin(): StringBuilder = append(
    """
    plugins {
        id("com.android.library")
        id("org.jetbrains.kotlin.android")
    }
    
    android {
        compileSdk = libs.versions.compileSdk.get().toInt()

        defaultConfig {
            minSdk = libs.versions.minSdk.get().toInt()
            targetSdk = libs.versions.targetSdk.get().toInt()
        }
    }
    
"""
        .trimIndent()
)

private fun StringBuilder.appendLibraryDependency(): StringBuilder = append(
    """
    dependencies {
        implementation(project(":&s:api"))
    }
    
"""
        .trimIndent()
)

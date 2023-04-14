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
        }
        
        androidComponents.beforeVariants { variant ->
            variant.enableAndroidTest = false
        }
    """.trimIndent()
)

fun StringBuilder.appendLibraryDependency(projectAccessor: String): StringBuilder = append(
    """
        dependencies {
            implementation(projects.$projectAccessor.api)
        }
    """.trimIndent()
)

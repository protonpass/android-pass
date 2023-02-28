package proton.android.pass.plugins.modulegen


fun StringBuilder.appendConfiguration(configuration: Configuration): StringBuilder =
    when (configuration) {
        Configuration.API -> appendJvmPlugin()
        Configuration.IMPL -> appendAndroidLibraryPlugin().appendLibraryDependency()
        Configuration.FAKES -> appendAndroidLibraryPlugin().appendLibraryDependency()
    }

private fun StringBuilder.appendJvmPlugin(): StringBuilder = append(
    """
    plugins {
        id("org.jetbrains.kotlin.jvm")
    }
    
""".trimIndent()
)

private fun StringBuilder.appendAndroidLibraryPlugin(): StringBuilder = append(
    """
    plugins {
        id("com.android.library")
        id("org.jetbrains.kotlin.android")
    }
    
    android {
        namespace = "&s2"
        compileSdk = libs.versions.compileSdk.get().toInt()

        defaultConfig {
            minSdk = libs.versions.minSdk.get().toInt()
            targetSdk = libs.versions.targetSdk.get().toInt()
        }
    }
    
""".trimIndent()
)

private fun StringBuilder.appendLibraryDependency(): StringBuilder = append(
    """
    dependencies {
        implementation(projects.&s1.api)
    }
    
""".trimIndent()
)

fun String.convertToProjectAccessor(): String {
    val builder = StringBuilder()
    var shouldCapitalizeNext = false
    for (c in this) {
        if (c == '-') {
            shouldCapitalizeNext = true
            continue
        }

        if (shouldCapitalizeNext) {
            builder.append(c.uppercase())
            shouldCapitalizeNext = false
        } else {
            builder.append(c)
        }
    }
    return builder.toString()
}

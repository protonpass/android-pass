package proton.android.pass.plugins.modulegen

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.File

open class ModuleGenTask : DefaultTask() {

    @Input
    @Option(
        option = "module",
        description = "Module path to be generated"
    )
    lateinit var moduleInput: String

    @Input
    @Option(
        option = "conf",
        description = "Configurations to be generated. api, impl, fakes"
    )
    lateinit var configurationInput: String

    @TaskAction
    fun generate() {
        val moduleList: List<String> = sanitizeModuleInput(moduleInput)
        if (moduleList.isEmpty()) {
            throw GradleException("Invalid module path: $moduleInput")
        }
        val detectedConfList: Set<Configuration> = detectConfigurations(configurationInput)
        if (detectedConfList.isEmpty()) {
            throw GradleException("Invalid configuration: $configurationInput")
        }

        logger.lifecycle("Sanitized module path: :${moduleList.joinToString(":")}")
        logger.lifecycle("Detected configurations: $detectedConfList")

        with(project) {
            generateDirs(moduleList, detectedConfList, ROOT_PACKAGE_NAME)
            generateManifest(moduleList, detectedConfList)
            generateBuildGradle(moduleList, detectedConfList, ROOT_PACKAGE_NAME)
            generateModuleSettings(moduleList, detectedConfList)
        }
    }

    private fun sanitizeModuleInput(path: String): List<String> {
        require(path.isNotBlank()) { "Module path is blank" }
        val moduleRegex = "[^-a-z]".toRegex()
        return path.split(":")
            .map { segment -> moduleRegex.replace(segment.lowercase(), "") }
            .filter(String::isNotEmpty)
    }

    private fun detectConfigurations(input: String): Set<Configuration> = input
        .split(",")
        .map { conf -> Configuration.valueOf(conf.uppercase()) }
        .toSet()

    private fun Project.generateManifest(
        modulePath: List<String>,
        configurationSet: Set<Configuration>
    ) {
        require(modulePath.isNotEmpty()) { "Module path is empty" }
        if (configurationSet.none { it == Configuration.IMPL || it == Configuration.FAKES }) {
            return
        }
        val manifestTemplate = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<manifest/>\n"
        configurationSet.forEach { conf ->
            val lcConfiguration = conf.name.lowercase()
            val dir = modulePath.joinToString(File.separator)
            val manifestFilePath = "$dir/$lcConfiguration/src/main/AndroidManifest.xml"
            file(manifestFilePath).writeText(manifestTemplate)
        }
    }

    private fun Project.generateDirs(
        modulePath: List<String>,
        configurationSet: Set<Configuration>,
        rootPackageName: String
    ) {
        val modulePathString = modulePath.joinToString(File.separator)
        val subPackagePath = modulePath.joinToString(File.separator)
        val rootPackagePath = rootPackageName.replace('.', '/')
        for (configuration in configurationSet) {
            val lcConfiguration = configuration.name.lowercase()
            val configurationPath = "$rootPackagePath/$subPackagePath/$lcConfiguration".replace("-", "")
            mkdir("$modulePathString/$lcConfiguration/src/main/kotlin/$configurationPath")
            if (configuration == Configuration.IMPL) {
                mkdir("$modulePathString/$lcConfiguration/src/androidTest/kotlin/$configurationPath")
                mkdir("$modulePathString/$lcConfiguration/src/test/kotlin/$configurationPath")
            }
        }
    }

    private fun Project.generateBuildGradle(
        moduleList: List<String>,
        configurationSet: Set<Configuration>,
        rootPackageName: String
    ) {
        val dir = moduleList.joinToString(File.separator)
        val subpackage = moduleList.joinToString(".")
        val subpackageWithoutHyphens = subpackage.replace("-", "")
        val asProjectAccessor = subpackage.convertToProjectAccessor()
        val namespace = "$rootPackageName.$subpackageWithoutHyphens"

        for (configuration in configurationSet) {
            val lcConfiguration = configuration.name.lowercase()

            val configString = when (configuration) {
                Configuration.API -> buildString {
                    appendJvmPlugin()
                    appendLine()
                }
                Configuration.IMPL,
                Configuration.FAKES -> buildString {
                    appendAndroidLibraryPlugin(namespace)
                    appendLine()
                    appendLine()
                    appendLibraryDependency(asProjectAccessor)
                    appendLine()
                }
            }

            file("$dir/${lcConfiguration}/build.gradle.kts").writeText(configString)
        }
    }

    private fun String.convertToProjectAccessor(): String =
        replace(Regex("-([a-z])")) { it.groupValues[1].uppercase() }

    private fun Project.generateModuleSettings(
        moduleList: List<String>,
        configurationSet: Set<Configuration>
    ) {
        val settingsFile = "settings.gradle.kts"
        val includePrefix = "include("
        val lines = file(settingsFile).readLines()
        val firstIncludeIndex = lines.indexOfFirst { it.startsWith(includePrefix) }
        if (firstIncludeIndex == -1) {
            throw IllegalStateException("Could not find $includePrefix in $settingsFile")
        }
        val existingIncludes = lines.filter { it.startsWith(includePrefix) }.toSet()
        val includeModulesPath = moduleList.joinToString(":")
        val newIncludes = configurationSet.map { configuration ->
            val lcConfiguration = configuration.name.lowercase()
            "$includePrefix\":$includeModulesPath:$lcConfiguration\")"
        }.toSet()
        val includes = existingIncludes + newIncludes
        val output = lines.take(firstIncludeIndex) +
            includes.sorted() +
            lines.takeLast(lines.size - (firstIncludeIndex + includes.size))
        file(settingsFile).writeText(output.joinToString(separator = "\n", postfix = "\n"))
    }

    companion object {
        const val ROOT_PACKAGE_NAME = "proton.android"
    }
}

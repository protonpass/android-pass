package proton.android.pass.plugins.modulegen

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.util.Locale

open class ModuleGenTask : DefaultTask() {

    private var moduleInput: String? = null
    private var configurationInput: String? = null

    @Option(
        option = "module",
        description = "Module path to be generated"
    )
    open fun setModule(moduleInput: String?) {
        this.moduleInput = moduleInput
    }

    @Option(
        option = "conf",
        description = "Configurations to be generated. api,impl,fakes"
    )
    open fun setConf(configurationInput: String?) {
        this.configurationInput = configurationInput
    }

    @Input
    open fun getModule(): String? = moduleInput

    @Input
    open fun getConf(): String? = configurationInput

    @Suppress("ThrowsCount")
    @TaskAction
    fun generate() {
        val module = moduleInput ?: throw GradleException("Module input cannot be null")
        val configurationList =
            configurationInput ?: throw GradleException("Configurations input cannot be null")

        val moduleRegex = "[^-a-z]".toRegex()
        val moduleList: List<String> = sanitizeModuleInput(module, moduleRegex)
        val detectedConfList: List<Configuration> = detectConfigurations(configurationList)
        if (moduleList.isEmpty()) {
            throw GradleException("Detected an empty module path")
        }
        if (detectedConfList.isEmpty()) {
            throw GradleException("Couldn't detect a proper configuration")
        }

        logger.lifecycle("Sanitized module path: :${moduleList.joinToString(":")}")
        logger.lifecycle("Detected configurations: $detectedConfList")

        with(project) {
            generateDirs(moduleList, detectedConfList)
            generateManifest(moduleList, detectedConfList)
            generateBuildGradle(moduleList, detectedConfList)
            generateModuleSettings(moduleList, detectedConfList)
        }
    }

    private fun Project.generateManifest(
        modulePath: List<String>,
        detectedConfList: List<Configuration>
    ) {
        detectedConfList.forEach { conf ->
            when (conf) {
                Configuration.IMPL,
                Configuration.FAKES -> {
                    val lcConfiguration = conf.name.lowercase()
                    val dir = modulePath.joinToString("/")
                    val manifestFile = "AndroidManifest.xml"
                    val manifestContent = """
                        <?xml version="1.0" encoding="utf-8"?>
                        <manifest/>
                        
                    """.trimIndent()
                    file("$dir/$lcConfiguration/src/main/$manifestFile").writeText(manifestContent)
                }
                else -> Unit
            }
        }
    }

    private fun sanitizeModuleInput(path: String, regex: Regex): List<String> = path.split(":")
        .map { segment -> regex.replace(segment.lowercase(), "") }
        .filter(String::isNotEmpty)

    private fun detectConfigurations(input: String): List<Configuration> = input
        .split(",")
        .map { conf -> Configuration.valueOf(conf.toUpperCase(Locale.ROOT)) }

    private fun Project.generateDirs(
        modulePath: List<String>,
        configurationList: List<Configuration>
    ) {
        val dir = modulePath.joinToString("/")
        val subpackage = modulePath.joinToString(".")
        configurationList
            .forEach { configuration ->
                val lcConfiguration = configuration.name.lowercase()
                val configurationPath = "$ROOT_PACKAGE_NAME.$subpackage.$lcConfiguration"
                    .replace('.', '/')
                    .replace("-", "")
                mkdir("$dir/$lcConfiguration/src/main/kotlin/$configurationPath")
                if (configuration == Configuration.IMPL) {
                    mkdir("$dir/$lcConfiguration/src/androidTest/kotlin/$configurationPath")
                    mkdir("$dir/$lcConfiguration/src/test/kotlin/$configurationPath")
                }
            }
    }

    private fun Project.generateBuildGradle(
        moduleList: List<String>,
        configurationList: List<Configuration>
    ) {
        val dir = moduleList.joinToString("/")
        val subpackage = moduleList.joinToString(".")
        val subpackageWithoutHyphens = subpackage.replace("-", "")
        val asProjectAccessor = subpackage.convertToProjectAccessor()
        configurationList
            .map { configuration ->
                val lcConfiguration = configuration.name.lowercase()
                val stringBuilder = StringBuilder()
                stringBuilder.appendConfiguration(configuration)
                configuration to stringBuilder.toString()
                    .replace("&s1", asProjectAccessor)
                    .replace("&s2", "$ROOT_PACKAGE_NAME.$subpackageWithoutHyphens.$lcConfiguration")
            }
            .forEach { pair ->
                file("$dir/${pair.first.name.lowercase()}/build.gradle.kts")
                    .writeText(pair.second)
            }
    }

    private fun Project.generateModuleSettings(
        moduleList: List<String>,
        configurationList: List<Configuration>
    ) {
        val settingsFile = "settings.gradle.kts"
        val includePrefix = "include("
        val fileLines = file(settingsFile).readLines()
        val firstIncludeIndex = fileLines.indexOfFirst { it.startsWith(includePrefix) }
        val includeList = fileLines.filter { it.startsWith(includePrefix) }.toMutableSet()
        val includeListSize = includeList.size

        val includeModulesPath = moduleList.joinToString(":")
        configurationList
            .forEach { configuration ->
                val lcConfiguration = configuration.name.lowercase()
                val include = "$includePrefix\":$includeModulesPath:$lcConfiguration\")"
                if (!includeList.contains(include)) {
                    includeList.add(include)
                }
            }
        val output = fileLines.take(firstIncludeIndex) +
            includeList.sorted() +
            fileLines.takeLast(fileLines.size - (firstIncludeIndex + includeListSize))
        file(settingsFile).writeText(output.joinToString(separator = "\n", postfix = "\n"))
    }

    companion object {
        const val ROOT_PACKAGE_NAME = "proton.android"
    }
}

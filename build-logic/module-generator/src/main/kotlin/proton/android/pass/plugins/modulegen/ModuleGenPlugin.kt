package proton.android.pass.plugins.modulegen

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

class ModuleGenPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.tasks.register<ModuleGenTask>("genModule")
    }
}

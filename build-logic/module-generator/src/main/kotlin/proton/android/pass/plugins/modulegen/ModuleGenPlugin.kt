package proton.android.pass.plugins.modulegen

import org.gradle.api.Plugin
import org.gradle.api.Project

class ModuleGenPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.tasks.register("genModule", ModuleGenTask::class.java)
    }
}

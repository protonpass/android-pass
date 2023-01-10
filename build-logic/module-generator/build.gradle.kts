
plugins {
    `kotlin-dsl`
}

group = "proton.android.pass.plugins.modulegen"

gradlePlugin {
    plugins {
        register("moduleGen") {
            id = "proton.android.pass.module-gen"
            implementationClass = "proton.android.pass.plugins.modulegen.ModuleGenPlugin"
        }
    }
}

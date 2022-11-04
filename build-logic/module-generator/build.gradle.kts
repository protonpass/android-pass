
plugins {
    `kotlin-dsl`
}

group = "me.proton.android.pass.plugins.modulegen"

gradlePlugin {
    plugins {
        register("moduleGen") {
            id = "me.proton.android.pass.module-gen"
            implementationClass = "me.proton.android.pass.plugins.modulegen.ModuleGenPlugin"
        }
    }
}

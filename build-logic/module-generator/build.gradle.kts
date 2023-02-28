import org.jetbrains.kotlin.samWithReceiver.gradle.SamWithReceiverExtension
import org.jetbrains.kotlin.samWithReceiver.gradle.SamWithReceiverGradleSubplugin

plugins {
    `embedded-kotlin`
    id("java-gradle-plugin")
}

plugins.apply(SamWithReceiverGradleSubplugin::class.java)
extensions.configure(SamWithReceiverExtension::class.java) {
    annotation(HasImplicitReceiver::class.qualifiedName!!)
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

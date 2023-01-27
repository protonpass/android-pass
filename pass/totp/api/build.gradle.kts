plugins {
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    implementation(projects.pass.common.api)
    implementation(libs.kotlinx.coroutines.core)
}

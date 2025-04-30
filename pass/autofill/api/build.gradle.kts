plugins {
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(projects.pass.data.api)
    implementation(projects.pass.domain)
}

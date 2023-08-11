plugins {
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    compileOnly(libs.compose.stable.marker)

    implementation(projects.pass.common.api)
}

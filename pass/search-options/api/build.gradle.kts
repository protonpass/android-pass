plugins {
    id("org.jetbrains.kotlin.jvm")
}   

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(projects.pass.domain)

    compileOnly(libs.compose.stable.marker)
}

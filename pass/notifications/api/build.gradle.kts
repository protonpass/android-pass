plugins {
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    implementation(libs.androidx.annotation)
    implementation(libs.kotlinx.coroutines.core)
    compileOnly(libs.compose.stable.marker)

    implementation(projects.pass.common.api)
    implementation(projects.pass.domain)
}

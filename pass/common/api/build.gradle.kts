plugins {
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    implementation(libs.core.network.domain)
    implementation(libs.core.utilKotlin)
    implementation(libs.kotlinx.coroutines.core)
}

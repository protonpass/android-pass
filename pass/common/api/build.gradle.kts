plugins {
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)

    api(libs.core.network.domain)
    api(libs.core.utilKotlin)
}

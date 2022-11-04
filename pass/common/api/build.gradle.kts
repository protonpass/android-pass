plugins {
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    implementation(libs.core.network.domain)
    implementation(libs.kotlinx.coroutines.core)
}

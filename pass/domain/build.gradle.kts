plugins {
    id("org.jetbrains.kotlin.jvm")
    kotlin("plugin.serialization")
}

dependencies {
    api(libs.core.cryptoCommon)
    api(libs.core.key.domain)
    api(projects.pass.common.api)

    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)
}

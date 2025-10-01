plugins {
    id("org.jetbrains.kotlin.jvm")
    kotlin("plugin.serialization")
}

dependencies {
    api(libs.core.cryptoCommon)
    api(libs.core.key.domain)
    api(projects.pass.common.api)

    compileOnly(libs.compose.stable.marker)
    implementation(libs.core.user.domain)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.core.accountManager.domain)
    implementation(libs.core.plan.domain)
}

plugins {
    id("org.jetbrains.kotlin.jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime)
    implementation(libs.core.account.domain)
    implementation(libs.core.cryptoCommon)
    implementation(libs.core.domain)
    implementation(libs.core.report.domain)
    implementation(libs.core.user.domain)
    implementation(libs.core.userSettings.domain)

    implementation(projects.pass.common.api)
    implementation(projects.pass.commonRust.api)
    implementation(projects.pass.crypto.api)
    implementation(projects.pass.domain)
    implementation(projects.pass.telemetry.api)

    testImplementation(libs.kotlinTest)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.junit)
}

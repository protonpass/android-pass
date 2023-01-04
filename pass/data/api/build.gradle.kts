plugins {
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.core.account.domain)
    implementation(libs.core.cryptoCommon)
    implementation(libs.core.domain)
    implementation(libs.core.user.domain)

    implementation(projects.pass.common.api)
    implementation(projects.pass.domain)

    testImplementation(libs.kotlinTest)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.junit)
}

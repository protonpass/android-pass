plugins {
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    implementation(libs.core.key.domain)
    implementation(libs.core.user.domain)

    implementation(projects.pass.domain)
    implementation(projects.pass.protos)
}

plugins {
    id("org.jetbrains.kotlin.jvm")
}   

dependencies {
    implementation(projects.pass.common.api)
    implementation(projects.pass.domain)
}

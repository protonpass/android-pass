plugins {
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    api(projects.pass.domain)
    api(projects.pass.protos)

    implementation(libs.kotlinx.datetime)
}

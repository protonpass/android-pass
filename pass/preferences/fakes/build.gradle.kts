plugins {
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)

    implementation(project(":pass:common:api"))
    implementation(project(":pass:preferences:api"))
}

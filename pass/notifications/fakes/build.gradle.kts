plugins {
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    implementation(projects.pass.common.api)

    api(projects.pass.notifications.api)

    implementation(libs.kotlinx.coroutines.core)
}

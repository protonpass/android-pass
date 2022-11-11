plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "me.proton.android.pass.notifications.fakes"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
    }
}
dependencies {
    implementation(project(":pass:common:api"))
    implementation(project(":pass:notifications:api"))

    implementation(libs.kotlinx.coroutines.core)
}

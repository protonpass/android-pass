plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "proton.android.pass.commonuimodels.api"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

androidComponents.beforeVariants { variant ->
    variant.enableAndroidTest = false
}

dependencies {
    implementation(projects.pass.domain)
    implementation(projects.pass.protos)

    implementation(libs.kotlinx.datetime)
}

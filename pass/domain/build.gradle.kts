plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("kotlin-parcelize")
    kotlin("plugin.serialization")
    id("dagger.hilt.android.plugin")
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    namespace = "me.proton.pass.domain"

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
        getByName("test").java.srcDirs("src/test/kotlin")
        getByName("androidTest").java.srcDirs("src/androidTest/kotlin")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.core.account)
    implementation(libs.core.accountManager)
    implementation(libs.core.crypto)
    implementation(libs.core.domain)
    implementation(libs.core.key)
    implementation(libs.core.user)

    implementation(projects.pass.common.api)
    implementation(projects.pass.log)

    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.android.compiler)
    kapt(libs.androidx.hilt.compiler)
}

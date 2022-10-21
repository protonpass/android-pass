plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("dagger.hilt.android.plugin")
    id("kotlin-parcelize")
}

android {
    compileSdk = Config.compileSdk
    buildFeatures.buildConfig = false

    defaultConfig {
        minSdk = Config.minSdk
        targetSdk = Config.targetSdk
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

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
    }

    hilt {
        enableAggregatingTask = true
    }
}

dependencies {
    implementation(libs.accompanist.navigation.animation)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.autofill)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundationLayout)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.uiTooling)
    implementation(libs.androidx.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.core.domain)
    implementation(libs.core.key)
    implementation(libs.core.presentation)
    implementation(libs.core.presentation.compose)
    implementation(libs.core.user)
    implementation(libs.core.utilKotlin)
    implementation(libs.kotlinx.coroutines.core)
    implementation(project(":passwordManager:common:api"))
    implementation(project(":passwordManager:data"))
    implementation(project(":passwordManager:domain"))
    implementation(project(":passwordManager:notifications:api"))
    implementation(project(":passwordManager:presentation"))
    implementation(project(":passwordManager:search:api"))
    implementation(project(":passwordManager:log"))

    testImplementation(project(":passwordManager:test"))
    testImplementation(libs.turbine)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinTest)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.junit)
    testImplementation(libs.core.test.kotlin)

    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.android.compiler)
    kapt(libs.androidx.hilt.compiler)
}

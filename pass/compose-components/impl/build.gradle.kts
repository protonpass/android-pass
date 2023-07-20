plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "proton.android.pass.composecomponents.impl"
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

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
    }
}

androidComponents.beforeVariants { variant ->
    variant.enableAndroidTest = false
}

dependencies {
    implementation(libs.accompanist.placeholder)
    implementation(libs.accompanist.navigation.material)
    implementation(libs.accompanist.systemUiController)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundationLayout)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui)
    implementation(libs.coil.compose)
    implementation(libs.core.presentation)
    implementation(libs.core.presentation.compose)
    implementation(libs.kotlinx.collections)
    implementation(libs.kotlinx.datetime)

    debugImplementation(libs.androidx.compose.uiTooling)
    implementation(libs.androidx.compose.uiToolingPreview)

    implementation(projects.pass.navigation.api)
    implementation(projects.pass.notifications.api)
    implementation(projects.pass.preferences.api)
    implementation(projects.pass.featureSearchOptions.api)
    implementation(projects.pass.common.api)
    implementation(projects.pass.commonUi.api)
    implementation(projects.pass.commonUiModels.api)
    implementation(projects.pass.domain)
    implementation(projects.pass.protos)

    debugImplementation(libs.showkase)
    kspDebug(libs.showkaseProcessor)

    testImplementation(libs.truth)
    testImplementation(libs.kotlinTest)
    testImplementation(libs.junit)
}

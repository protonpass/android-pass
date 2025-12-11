plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    alias(libs.plugins.gradlePlugin.compose.compiler)
}

android {
    namespace = "proton.android.pass.composecomponents.impl"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        
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
}

androidComponents.beforeVariants { variant ->
    variant.enableAndroidTest = false
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.core.compose)
    debugImplementation(libs.androidx.compose.uiTooling)

    implementation(libs.accompanist.placeholder)
    implementation(libs.accompanist.navigation.material)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.annotation)

    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.coil.compose)
    implementation(libs.core.presentation)
    implementation(libs.core.presentation.compose)
    implementation(libs.kotlinx.collections)
    implementation(libs.kotlinx.datetime)

    implementation(projects.pass.common.api)
    implementation(projects.pass.commonPresentation.impl)
    implementation(projects.pass.commonUi.api)
    implementation(projects.pass.commonRust.api)
    implementation(projects.pass.commonUiModels.api)
    implementation(projects.pass.domain)
    implementation(projects.pass.searchOptions.api)
    implementation(projects.pass.inAppReview.api)
    implementation(projects.pass.log.api)
    implementation(projects.pass.navigation.api)
    implementation(projects.pass.notifications.api)
    implementation(projects.pass.preferences.api)
    implementation(projects.pass.protos)

    debugImplementation(libs.showkase)
    kspDebug(libs.showkaseProcessor)

    testImplementation(libs.truth)
    testImplementation(libs.kotlinTest)
    testImplementation(libs.junit)
}

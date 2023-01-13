plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("app.cash.paparazzi")
    id("com.google.devtools.ksp")
}

android {
    compileSdk = 31
    namespace = "proton.android.pass.screenshottests"
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = 31
    }
    flavorDimensions += "default"
    productFlavors {
        maybeCreate("dev")
        maybeCreate("alpha")
        maybeCreate("prod")
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
    }
    sourceSets {
        getByName("test").java.srcDirs("build/generated/ksp/devDebugUnitTest/kotlin")
    }
}

androidComponents {
    beforeVariants(selector().withBuildType("release")) { builder ->
        builder.enable = false
    }
}

dependencies {
    implementation(projects.pass.autofill.impl)
    implementation(projects.pass.presentation)
    implementation(projects.pass.composeComponents.impl)
    implementation(projects.pass.commonUi.api)
    implementation(projects.pass.featureAuth.impl)
    implementation(projects.pass.featureCreateItem.impl)
    implementation(projects.pass.featureHome.impl)

    testImplementation(libs.androidx.compose.ui)
    testImplementation(libs.androidx.compose.uiTooling)
    testImplementation(libs.core.presentation.compose)

    testImplementation(libs.showkase)
    kspTest(libs.showkaseProcessor)

    testImplementation(libs.testParameterInjector)
}

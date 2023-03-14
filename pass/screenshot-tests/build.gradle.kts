plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("app.cash.paparazzi")
    id("com.google.devtools.ksp")
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    namespace = "proton.android.pass.screenshottests"
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
    }
    flavorDimensions += "version"
    productFlavors {
        maybeCreate("dev")
        maybeCreate("alpha")
        maybeCreate("play")
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
    implementation(projects.pass.composeComponents.impl)
    implementation(projects.pass.commonUi.api)
    implementation(projects.pass.featureAuth.impl)
    implementation(projects.pass.featureItemCreate.impl)
    implementation(projects.pass.featureHome.impl)
    implementation(projects.pass.featureSettings.impl)
    implementation(projects.pass.featureVault.impl)
    implementation(projects.pass.featureOnboarding.impl)
    implementation(projects.pass.featureItemDetail.impl)
    implementation(projects.pass.featureTrash.impl)
    implementation(projects.pass.featureProfile.impl)

    testImplementation(libs.androidx.compose.ui)
    testImplementation(libs.androidx.compose.uiTooling)
    testImplementation(libs.core.presentation.compose)

    testImplementation(libs.showkase)
    kspTest(libs.showkaseProcessor)

    testImplementation(libs.testParameterInjector)
}

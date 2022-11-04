plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("app.cash.paparazzi")
    id("com.google.devtools.ksp")
}

android {
    compileSdk = 31
    namespace = "me.proton.android.pass.screenshottests"
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
    implementation(project(":pass:presentation"))
    implementation(project(":pass:common-ui:api"))

    testImplementation(libs.androidx.compose.ui)
    testImplementation(libs.androidx.compose.uiTooling)
    testImplementation(libs.core.presentation.compose)

    testImplementation(libs.showkase)
    kspTest(libs.showkaseProcessor)

    testImplementation(libs.testParameterInjector)
}

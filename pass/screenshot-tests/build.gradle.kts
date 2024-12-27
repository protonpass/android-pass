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
        
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
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
    beforeVariants { variant ->
        variant.enableAndroidTest = false
    }
}

dependencies {
    implementation(projects.pass.autofill.impl)
    implementation(projects.pass.commonUi.api)
    implementation(projects.pass.composeComponents.impl)
    implementation(projects.pass.features.account)
    implementation(projects.pass.features.auth)
    implementation(projects.pass.features.attachments)
    implementation(projects.pass.featureHome.impl)
    implementation(projects.pass.features.itemCreate)
    implementation(projects.pass.features.itemDetail)
    implementation(projects.pass.features.migrate)
    implementation(projects.pass.features.onboarding)
    implementation(projects.pass.features.passkeys)
    implementation(projects.pass.features.profile)
    implementation(projects.pass.features.searchOptions)
    implementation(projects.pass.features.aliasContacts)
    implementation(projects.pass.features.password)
    implementation(projects.pass.features.selectItem)
    implementation(projects.pass.features.settings)
    implementation(projects.pass.features.sharing)
    implementation(projects.pass.features.sync)
    implementation(projects.pass.features.trash)
    implementation(projects.pass.features.trial)
    implementation(projects.pass.features.vault)
    implementation(projects.pass.features.itemHistory)
    implementation(projects.pass.features.itemOptions)
    implementation(projects.pass.features.inAppMessages)
    implementation(projects.pass.features.secureLinks)
    implementation(projects.pass.features.securityCenter)
    implementation(projects.pass.features.slSync)
    implementation(projects.pass.features.report)
    implementation(projects.pass.features.upsell)

    testImplementation(libs.androidx.compose.ui)
    testImplementation(libs.androidx.compose.uiTooling)
    testImplementation(libs.core.presentation.compose)
    testImplementation(libs.kotlin.reflect)

    testImplementation(libs.showkase)
    kspTest(libs.showkaseProcessor)

    testImplementation(libs.testParameterInjector)
}

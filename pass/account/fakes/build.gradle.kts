plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("dagger.hilt.android.plugin")
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "proton.android.pass.account.fakes"
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
}

androidComponents.beforeVariants { variant ->
    variant.enableAndroidTest = false
}

dependencies {
    api(projects.pass.account.api)

    implementation(libs.core.cryptoCommon)
    implementation(libs.core.accountManager.domain)
    implementation(libs.core.deviceMigrationDomain)
    implementation(libs.core.payment.domain)
    implementation(libs.core.authFidoDomain)
    implementation(libs.core.userSettings.domain)
    implementation(libs.core.telemetry.domain)

    implementation(libs.androidx.activity)

    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.android.compiler)
    kapt(libs.androidx.hilt.compiler)

    implementation(projects.pass.crypto.api)
}

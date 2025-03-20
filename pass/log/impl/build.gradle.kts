plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("dagger.hilt.android.plugin")
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "proton.android.pass.log.impl"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        
    }

    flavorDimensions += "version"
    productFlavors {
        maybeCreate("dev")
        maybeCreate("alpha")
        maybeCreate("play")
        maybeCreate("fdroid")
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

fun DependencyHandlerScope.addFdroidSpecialLib(
    default: Any,
    fdroid: Any?
) {

    val devImplementation = configurations.getByName("devImplementation")
    val alphaImplementation = configurations.getByName("alphaImplementation")
    val playImplementation = configurations.getByName("playImplementation")
    val fdroidImplementation = configurations.getByName("fdroidImplementation")

    devImplementation(default)
    alphaImplementation(default)
    playImplementation(default)

    fdroid?.let { dep ->
        fdroidImplementation(dep)
    }
}

dependencies {
    api(projects.pass.log.api)
    
    implementation(projects.pass.common.api)

    addFdroidSpecialLib(
        default = projects.pass.tracing.impl,
        fdroid = projects.pass.tracing.fdroid
    )

    implementation(projects.pass.appConfig.api)

    implementation(libs.androidx.startup.runtime)
    implementation(libs.timber)
    implementation(libs.core.utilKotlin)
    implementation(libs.kotlinx.datetime)

    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.android.compiler)
    kapt(libs.androidx.hilt.compiler)
}

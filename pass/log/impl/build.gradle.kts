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
        maybeCreate("quest")
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

fun DependencyHandlerScope.addSpecialLib(
    default: Any,
    overrides: Map<String, Any?> = emptyMap()
) {
    val variants = listOf("dev", "alpha", "play", "quest", "fdroid")

    variants.forEach { variant ->
        val dependency = overrides[variant] ?: default
        dependency.let {
            val config = configurations.getByName("${variant}Implementation")
            config(it)
        }
    }
}

dependencies {
    api(projects.pass.log.api)
    
    implementation(projects.pass.common.api)

    addSpecialLib(
        default = projects.pass.tracing.impl,
        overrides = mapOf(
            "fdroid" to projects.pass.tracing.noOp
        )
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

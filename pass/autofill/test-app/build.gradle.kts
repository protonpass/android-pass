plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-android")
}

val appVersionName = "1.7.0"
val appVersionCode = versionCode(appVersionName)
val archivesBaseName = "AutofillTestApp-$appVersionName"

fun versionCode(versionName: String): Int {
    val segment = versionName.split('.').map { it.toInt() }
    return (segment[0] * 10000000) + (segment[1] * 100000) + (segment[2] * 1000)
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    namespace = "proton.android.pass.autofill.sample"

    defaultConfig {
        applicationId = "proton.android.pass.autofill.sample"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = appVersionCode
        versionName = appVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
        getByName("test").java.srcDirs("src/test/kotlin")
        getByName("androidTest").java.srcDirs("src/androidTest/kotlin")
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            isTestCoverageEnabled = false
        }
        release {
            isMinifyEnabled = true
            //isShrinkResources = true // should be replaced by useResourceShrinker
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
        }
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
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
    }

    packagingOptions {
        resources.excludes.add("META-INF/licenses/**")
        resources.excludes.add("META-INF/AL2.0")
        resources.excludes.add("META-INF/LGPL2.1")
    }
}

dependencies {
    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundationLayout)
    implementation(libs.androidx.compose.foundationLayout)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.core.presentation.compose)
    implementation(libs.material)
}

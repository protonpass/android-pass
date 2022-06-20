plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-android")
}

android {
    compileSdk = Config.compileSdk

    defaultConfig {
        applicationId = "me.proton.pass.core.autofill.sample"
        minSdk = 27 // Needed for autofill
        targetSdk = Config.targetSdk
        versionCode = Config.versionCode
        versionName = Config.versionName
        testInstrumentationRunner = Config.testInstrumentationRunner
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
            isTestCoverageEnabled = true
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
        viewBinding {
            isEnabled = true
        }
    }

    composeOptions {
        kotlinCompilerExtensionVersion = Versions.AndroidX.compose
    }

    packagingOptions {
        resources.excludes.add("META-INF/licenses/**")
        resources.excludes.add("META-INF/AL2.0")
        resources.excludes.add("META-INF/LGPL2.1")
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
    implementation(Dependencies.passAutofillSampleLibs)
    testImplementation(Dependencies.testLibs)
    androidTestImplementation(Dependencies.androidTestLibs)
}

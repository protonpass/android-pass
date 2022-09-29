plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
    id("kotlin-parcelize")
    kotlin("plugin.serialization") version Versions.Gradle.kotlinGradlePlugin
}

android {
    compileSdk = Config.compileSdk

    defaultConfig {
        minSdk = 26
        targetSdk = Config.targetSdk
        testInstrumentationRunner = Config.testInstrumentationRunner
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
        getByName("test").java.srcDirs("src/test/kotlin")
        getByName("androidTest").java.srcDirs("src/androidTest/kotlin")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }

    packagingOptions {
        resources.excludes.add("**/attach_hotspot_windows.dll")
        resources.excludes.add("META-INF/licenses/**")
        resources.excludes.add("META-INF/AL2.0")
        resources.excludes.add("META-INF/LGPL2.1")
    }

    hilt {
        enableAggregatingTask = true
    }
}

dependencies {
    implementation(Dependencies.passAutofillServiceLibs)
    implementation(project(":passwordManager:domain"))
    testImplementation(Dependencies.testLibs)
    androidTestImplementation(Dependencies.androidTestLibs)
}

setAsHiltModule()

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
    id("kotlin-parcelize")
}

base {
    archivesName.set(Config.archivesBaseName)
}

tasks.register("getArchivesName"){
    doLast {
        println("[ARCHIVES_NAME]${Config.archivesBaseName}")
    }
}

android {
    compileSdk = Config.compileSdk
    buildToolsVersion = Config.buildTools

    defaultConfig {
        applicationId = Config.applicationId
        minSdk = Config.minSdk
        targetSdk = Config.targetSdk
        versionCode = Config.versionCode
        versionName = Config.versionName
        testInstrumentationRunner = Config.testInstrumentationRunner
        buildConfigField("String", "HOST", "\"protonmail.com\"")
        buildConfigField("String", "BASE_URL", "\"https://drive.protonmail.com/api/\"")
        buildConfigField("String", "ENVIRONMENT", "\"api.protonmail.com\"")
        buildConfigField("String", "FLAVOR_DEVELOPMENT", "\"dev\"")
        buildConfigField("String", "FLAVOR_PRODUCTION", "\"prod\"")
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
        }
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

    flavorDimensions.add("default")
    productFlavors {
        create("dev") {
            applicationIdSuffix = ".dev"
            val gitHash = "git rev-parse --short HEAD".runCommand(workingDir = rootDir)
            versionNameSuffix = "-dev (${gitHash})"
            buildConfigField("String", "HOST", "\"proton.black\"")
            buildConfigField("String", "BASE_URL", "\"https://drive.proton.black/api/\"")
            buildConfigField("String", "ENVIRONMENT", "\"api.proton.black\"")
        }
        create("prod") {
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }

    buildFeatures {
        compose = true
        dataBinding = true // required by Core presentation
    }

    composeOptions {
        kotlinCompilerExtensionVersion = Versions.AndroidX.compose
    }

    hilt {
        enableAggregatingTask = true
    }

    packagingOptions {
        resources.excludes.add("META-INF/licenses/**")
        resources.excludes.add("META-INF/AL2.0")
        resources.excludes.add("META-INF/LGPL2.1")
    }

    kapt {
        correctErrorTypes = true
    }
}

kotlin.sourceSets.all {
    languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
    languageSettings.useExperimentalAnnotation("kotlinx.coroutines.ExperimentalCoroutinesApi")
}

tasks.create("publishGeneratedReleaseNotes") {
    doLast {
        val releaseNotesDir = File("${project.projectDir}/src/main/play/release-notes/en-US")
        releaseNotesDir.mkdirs()
        val releaseNotesFile = File(releaseNotesDir, "default.txt")
        // Limit of 500 chars on Google Play console for release notes
        releaseNotesFile.writeText(
            generateChangelog(
                rootDir,
                since = System.getenv("CI_COMMIT_BEFORE_SHA")
            ).let { changelog ->
                if (changelog.length <= 490) {
                    changelog
                } else {
                    ("${changelog.take(490)}...")
                }
            })
    }
}

tasks.create("printGeneratedChangelog") {
    doLast {
        println(generateChangelog(rootDir, since = System.getProperty("since")))
    }
}

configurations.all {
    // androidx.test includes junit 4.12 so this will force that entire project uses same junit version
    resolutionStrategy.force("junit:junit:${Versions.Test.junit}")
    // TODO: Remove this once Core migrates to kotlin 1.5 and updated store version
    resolutionStrategy.force("com.dropbox.mobile.store:store4:4.0.2-KT15")
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
    implementation(Dependencies.appLibs)
    implementation(project(":passwordManager:dagger"))
    implementation(project(":passwordManager:data"))
    implementation(project(":passwordManager:domain"))
    implementation(project(":passwordManager:presentation"))
    implementation(project(":autofill:service"))
    debugImplementation(Dependencies.appDebug)
    kapt(Dependencies.appAnnotationProcessors)
    testImplementation(Dependencies.testLibs)
    androidTestImplementation(Dependencies.androidTestLibs)
}

configureJacoco(flavor = "dev")
setAsHiltModule()

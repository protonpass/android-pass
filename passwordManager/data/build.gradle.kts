// Necessary imports for the protobuf section to work
import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.plugins
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc
import org.apache.tools.ant.taskdefs.condition.Os

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    kotlin("plugin.serialization") version Versions.Gradle.kotlinGradlePlugin
    id("com.google.protobuf") version Versions.Gradle.protobufPlugin
}

// Necessary hack for the Protobuf plugin until they support AGP 7.x
// https://github.com/google/protobuf-gradle-plugin/issues/540
fun com.android.build.api.dsl.AndroidSourceSet.proto(action: SourceDirectorySet.() -> Unit) {
    (this as? ExtensionAware)
        ?.extensions
        ?.getByName("proto")
        ?.let { it as? SourceDirectorySet }
        ?.apply(action)
}

android {
    compileSdk = Config.compileSdk

    defaultConfig {
        minSdk = Config.minSdk
        targetSdk = Config.targetSdk
        testInstrumentationRunner = Config.testInstrumentationRunner
    }

    sourceSets {
        getByName("main") {
            java.srcDirs("src/main/kotlin")
            proto {
                srcDir("contents-proto-definition/protos")
            }
        }
        getByName("test") {
            java.srcDirs("src/test/kotlin")
        }
        getByName("androidTest") {
            java.srcDirs("src/androidTest/kotlin")
        }
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
}

dependencies {
    compileOnly(files("../../proton-libs/gopenpgp/gopenpgp.aar"))
    implementation(Dependencies.passDataLibs)
    kapt(Dependencies.passDataAnnotationProcessors)
    implementation(project(":passwordManager:domain"))
    implementation("com.google.protobuf:protobuf-lite:${Versions.Protobuf.javaliteArtifact}")

    androidTestImplementation(files("../../proton-libs/gopenpgp/gopenpgp.aar"))
    androidTestImplementation(project(":passwordManager:test"))
    androidTestImplementation(Dependencies.androidTestLibs)
}

protobuf {
    val archSuffix = if (Os.isFamily(Os.FAMILY_MAC)) ":osx-x86_64" else ""
    protoc {
        artifact = "com.google.protobuf:protoc:${Versions.Protobuf.protocArtifact}$archSuffix"
    }
    plugins {
        id("javalite") {
            artifact =
                "com.google.protobuf:protoc-gen-javalite:${Versions.Protobuf.javaliteArtifact}$archSuffix"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                id("javalite") { }
            }
        }
    }
}

//configureJacoco()
setAsHiltModule()

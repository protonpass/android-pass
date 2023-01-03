import com.google.protobuf.gradle.builtins
import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.ofBuildType
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("dagger.hilt.android.plugin")
    id("com.google.protobuf")
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    namespace = "me.proton.android.pass.preferences.implementation"

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
    }
    sourceSets {
        getByName("debug") {
            java.srcDirs("build/generated/source/proto/debug")
        }
        getByName("release") {
            java.srcDirs("build/generated/source/proto/release")
        }
    }
}

protobuf {
    protoc {
        artifact = project.libs.google.protobuf.protoc.get().toString()
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                val java by registering {
                    option("lite")
                }
            }
        }
        ofBuildType("release")
    }
}

dependencies {
    implementation(libs.androidx.datastore)
    implementation(libs.core.utilKotlin)
    implementation(libs.google.protobuf.lite)
    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.android.compiler)
    kapt(libs.androidx.hilt.compiler)

    implementation(projects.pass.log.api)

    api(projects.pass.preferences.api)
}

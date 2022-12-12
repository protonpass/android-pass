import com.google.protobuf.gradle.builtins
import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("com.google.devtools.ksp")
    kotlin("plugin.serialization")
    id("com.google.protobuf")
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
    namespace = "me.proton.android.pass.protos"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        testInstrumentationRunner = Config.testInstrumentationRunner
    }

    sourceSets {
        getByName("main") {
            java.srcDirs("src/main/kotlin")
            java.srcDirs("build/generated/source/proto")
            proto {
                srcDir("contents-proto-definition/protos")
            }
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
    implementation(libs.google.protobuf.lite)
}


protobuf {
    val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
    protoc {
        artifact = "${catalog.findLibrary("google.protobuf.protoc").get().get()}"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                val java by registering {
                    option("lite")
                }
                val kotlin by registering {
                    option("lite")
                }
            }
        }
    }
}

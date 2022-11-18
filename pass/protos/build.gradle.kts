import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.plugins
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
    val archSuffix = if (org.apache.tools.ant.taskdefs.condition.Os.isFamily(org.apache.tools.ant.taskdefs.condition.Os.FAMILY_MAC)) ":osx-x86_64" else ""
    val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
    protoc {
        artifact = "${catalog.findLibrary("google.protobuf.protoc").get().get()}$archSuffix"
    }
    plugins {
        id("javalite") {
            artifact =
                "${catalog.findLibrary("google.protobuf.protocGenJavalite").get().get()}$archSuffix"
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

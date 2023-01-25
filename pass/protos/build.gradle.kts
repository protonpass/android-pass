import com.google.protobuf.gradle.builtins
import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("com.google.protobuf")
}

sourceSets {
    getByName("main") {
        proto {
            srcDir("contents-proto-definition/protos")
        }
        java.srcDirs(
            "${protobuf.protobuf.generatedFilesBaseDir}/main/java",
            "${protobuf.protobuf.generatedFilesBaseDir}/main/kotlin"
        )
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

dependencies {
    api(libs.google.protobuf.kotlin.lite)
}

protobuf {
    protoc {
        artifact = project.libs.google.protobuf.protoc.get().toString()
    }
    generateProtoTasks {
        all().configureEach {
            builtins {
                named("java") {
                    option("lite")
                }
                id("kotlin")
            }
        }
    }
}

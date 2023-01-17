import com.google.protobuf.gradle.builtins
import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.ofSourceSet
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("com.google.protobuf")
}

sourceSets {
    main {
        proto {
            srcDir("contents-proto-definition/protos")
        }
        java {
            srcDirs("build/generated/source/proto/main")
        }
    }
}

dependencies {
    api(libs.google.protobuf.lite)
}

protobuf {
    protoc {
        artifact = project.libs.google.protobuf.protoc.get().toString()
    }
    generateProtoTasks {
        ofSourceSet("main")
            .first()
            .builtins {
                getByName("java").option("lite")
            }
    }
}

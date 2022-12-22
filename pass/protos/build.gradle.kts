import com.google.protobuf.gradle.*

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

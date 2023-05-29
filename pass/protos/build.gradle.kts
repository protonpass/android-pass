import com.google.protobuf.gradle.id

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
            "${protobuf.generatedFilesBaseDir}/main/java",
            "${protobuf.generatedFilesBaseDir}/main/kotlin"
        )
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
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
        all().forEach { task ->
            task.builtins {
                named("java") {
                    option("lite")
                }
                id("kotlin")
            }
        }
    }
}

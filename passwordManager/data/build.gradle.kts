// Necessary imports for the protobuf section to work
import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.plugins
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc
import org.apache.tools.ant.taskdefs.condition.Os

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("com.google.devtools.ksp")
    kotlin("plugin.serialization")
    id("com.google.protobuf")
    id("dagger.hilt.android.plugin")
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
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
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
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.core.account)
    implementation(libs.core.accountManager)
    implementation(libs.core.crypto)
    implementation(libs.core.data)
    implementation(libs.core.dataRoom)
    implementation(libs.core.domain)
    implementation(libs.core.key)
    implementation(libs.core.network)
    implementation(libs.core.user)
    implementation(libs.core.utilKotlin)
    implementation(libs.retrofit)
    ksp(libs.androidx.room.compiler)
    implementation(libs.google.protobuf.lite)

    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.android.compiler)
    kapt(libs.androidx.hilt.compiler)

    implementation(project(":passwordManager:common:api"))
    implementation(project(":passwordManager:domain"))
    implementation(project(":passwordManager:log"))

    testImplementation(libs.truth)
    testImplementation(libs.kotlinTest)
    testImplementation(libs.junit)
    testImplementation(libs.core.test.kotlin)
    testImplementation(project(":passwordManager:test"))

    androidTestImplementation(files("../../proton-libs/gopenpgp/gopenpgp.aar"))
    androidTestImplementation(project(":passwordManager:test"))

    androidTestImplementation(libs.androidx.compose.ui.test)
    androidTestImplementation(libs.androidx.compose.ui.test.junit)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.core.ktx)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.kotlinTest)
    androidTestImplementation(libs.core.test.android.instrumented)
}

protobuf {
    val archSuffix = if (Os.isFamily(Os.FAMILY_MAC)) ":osx-x86_64" else ""
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

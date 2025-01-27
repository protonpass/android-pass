import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.jvm")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.kotlinx.datetime)
    implementation(libs.core.key.domain)
    implementation(libs.core.user.domain)

    implementation(projects.pass.domain)
    implementation(projects.pass.protos)
}

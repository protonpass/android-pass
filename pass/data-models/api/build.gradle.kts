plugins {
    id("org.jetbrains.kotlin.jvm")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

dependencies {
    implementation(projects.pass.crypto.api)
    implementation(projects.pass.domain)
    implementation(projects.pass.protos)
}

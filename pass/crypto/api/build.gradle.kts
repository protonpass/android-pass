plugins {
    id("org.jetbrains.kotlin.jvm")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

dependencies {
    implementation(libs.kotlinx.datetime)
    implementation(libs.core.key.domain)
    implementation(libs.core.user.domain)

    implementation(projects.pass.domain)
    implementation(projects.pass.protos)

    implementation(libs.commons.codec)
}

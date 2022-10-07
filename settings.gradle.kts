
rootProject.name = "Password Manager"

// Use core libs from maven artifacts or from git submodule using Gradle's included build:
// - to enable/disable locally: gradle.properties > useCoreGitSubmodule
// - to enable/disable on CI: .gitlab-ci.yml > ORG_GRADLE_PROJECT_useCoreGitSubmodule
val coreSubmoduleDir = rootDir.resolve("proton-libs")
extra.set("coreSubmoduleDir", coreSubmoduleDir)
val includeCoreLibsHelper = File(coreSubmoduleDir, "gradle/include-core-libs.gradle.kts")
if (includeCoreLibsHelper.exists()) {
    apply(from = "${coreSubmoduleDir.path}/gradle/include-core-libs.gradle.kts")
} else if (extensions.extraProperties["useCoreGitSubmodule"].toString().toBoolean()) {
    includeBuild("proton-libs")
    println("Core libs from git submodule `$coreSubmoduleDir`")
}

include(":app")
include(":passwordManager:autofill:demo")
include(":passwordManager:autofill:implementation")
include(":passwordManager:dagger")
include(":passwordManager:data")
include(":passwordManager:domain")
include(":passwordManager:presentation")
include(":passwordManager:search:api")
include(":passwordManager:search:implementation")
include(":passwordManager:test")
include(":passwordManager:log")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

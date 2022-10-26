
rootProject.name = "Password Manager"

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("me.proton.core.gradle-plugins.include-core-build") version "1.0.0"
}

includeCoreBuild {
    uri.set("https://github.com/ProtonMail/protoncore_android.git")
    branch.set("main")
    includeBuild("gopenpgp")
}

include(":app")
include(":passwordManager:autofill:demo")
include(":passwordManager:autofill:implementation")
include(":passwordManager:common:api")
include(":passwordManager:common:implementation")
include(":passwordManager:common-ui:api")
include(":passwordManager:common-ui:implementation")
include(":passwordManager:data")
include(":passwordManager:domain")
include(":passwordManager:log")
include(":passwordManager:notifications:api")
include(":passwordManager:notifications:implementation")
include(":passwordManager:presentation")
include(":passwordManager:screenshot-tests")
include(":passwordManager:search:api")
include(":passwordManager:search:implementation")
include(":passwordManager:test")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

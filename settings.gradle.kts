rootProject.name = "ProtonPass"

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("me.proton.core.gradle-plugins.include-core-build") version "1.1.1"
}

includeCoreBuild {
    branch.set("main")
    includeBuild("gopenpgp")
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":app")
include(":pass:autofill:api")
include(":pass:autofill:demo")
include(":pass:autofill:fakes")
include(":pass:autofill:implementation")
include(":pass:biometry:api")
include(":pass:biometry:fakes")
include(":pass:biometry:implementation")
include(":pass:common-ui:api")
include(":pass:common-ui:implementation")
include(":pass:common:api")
include(":pass:common:implementation")
include(":pass:data:api")
include(":pass:data:fakes")
include(":pass:data:impl")
include(":pass:domain")
include(":pass:log")
include(":pass:navigation:api")
include(":pass:notifications:api")
include(":pass:notifications:fakes")
include(":pass:notifications:implementation")
include(":pass:preferences:api")
include(":pass:preferences:fakes")
include(":pass:preferences:implementation")
include(":pass:presentation")
include(":pass:protos")
include(":pass:screenshot-tests")
include(":pass:search:api")
include(":pass:search:fakes")
include(":pass:search:implementation")
include(":pass:test")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

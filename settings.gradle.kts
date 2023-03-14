rootProject.name = "ProtonPass"

fun getProtosUrl(): String {
    val isCI = System.getenv("CI").toBoolean()
    if (isCI) {
        val username = "gitlab-ci-token"
        val token = System.getenv("CI_JOB_TOKEN")
        return "https://${username}:${token}@gitlab.protontech.ch/proton/clients/pass/contents-proto-definition.git"
    } else {
        val username = "AndroidDeployToken"
        val token = "glpat-Poxe3LKtUJSqKXKgusac"
        return "https://${username}:${token}@gitlab.protontech.ch/proton/clients/pass/contents-proto-definition.git"
    }
}

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

    includeRepo("contents-proto-definition") {
        uri.set(getProtosUrl())
        branch.set("master")
        checkoutDirectory.set(file("./pass/protos/contents-proto-definition"))
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":app")
include(":benchmark")
include(":pass:app-config:api")
include(":pass:autofill:api")
include(":pass:autofill:e2e-app")
include(":pass:autofill:fakes")
include(":pass:autofill:impl")
include(":pass:autofill:test-app")
include(":pass:biometry:api")
include(":pass:biometry:fakes")
include(":pass:biometry:impl")
include(":pass:clipboard:api")
include(":pass:clipboard:fakes")
include(":pass:clipboard:impl")
include(":pass:common-test")
include(":pass:common-ui-models:api")
include(":pass:common-ui-models:fakes")
include(":pass:common-ui:api")
include(":pass:common:api")
include(":pass:compose-components:impl")
include(":pass:crypto:api")
include(":pass:crypto:fakes")
include(":pass:crypto:impl")
include(":pass:data:api")
include(":pass:data:fakes")
include(":pass:data:impl")
include(":pass:domain")
include(":pass:feature-auth:impl")
include(":pass:feature-item-create:impl")
include(":pass:feature-home:impl")
include(":pass:feature-item-detail:impl")
include(":pass:feature-onboarding:impl")
include(":pass:feature-profile:impl")
include(":pass:feature-settings:impl")
include(":pass:feature-trash:impl")
include(":pass:feature-vault:impl")
include(":pass:image:impl")
include(":pass:log:api")
include(":pass:log:impl")
include(":pass:navigation:api")
include(":pass:network:api")
include(":pass:network:impl")
include(":pass:notifications:api")
include(":pass:notifications:fakes")
include(":pass:notifications:impl")
include(":pass:preferences:api")
include(":pass:preferences:fakes")
include(":pass:preferences:impl")
include(":pass:protos")
include(":pass:screenshot-tests")
include(":pass:totp:api")
include(":pass:totp:fakes")
include(":pass:totp:impl")
include(":pass:tracing:impl")
include(":pass:tracing:impl")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

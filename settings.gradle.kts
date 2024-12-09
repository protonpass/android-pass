rootProject.name = "ProtonPass"

val localProperties = java.util.Properties().apply {
    try {
        load(rootDir.resolve("local.properties").inputStream())
    } catch (exception: java.io.FileNotFoundException) {
        // Provide empty properties to allow the app to be built without secrets
        java.util.Properties()
    }
}

interface BranchOrTag {
    data class Branch(val name: String) : BranchOrTag
    data class Tag(val name: String) : BranchOrTag
}

data class ProtosConfig(
    val url: String,
    val branchTag: BranchOrTag
)

val PROTOBUF_TAG = "1.4.0"

fun getProtosConfig(): ProtosConfig {
    val isCI = System.getenv("GITLAB_CI").toBoolean()
    val customProtosUrl = localProperties.getProperty("protos.url", "")
    return if (isCI) {
        val username = "gitlab-ci-token"
        val token = System.getenv("CI_JOB_TOKEN")
        val server = System.getenv("CI_SERVER_HOST")
        ProtosConfig(
            url = "https://${username}:${token}@${server}/proton/clients/pass/contents-proto-definition.git",
            branchTag = BranchOrTag.Tag(PROTOBUF_TAG)
        )
    } else if (customProtosUrl.isNotBlank()) {

        val customProtosBranch = localProperties.getProperty("protos.branch", "")
        val customProtosTag = localProperties.getProperty("protos.tag", "")

        val branchTag = when {
            customProtosBranch.isNotBlank() -> BranchOrTag.Branch(customProtosBranch)
            customProtosTag.isNotBlank() -> BranchOrTag.Tag(customProtosTag)
            else -> throw RuntimeException("Either protos.branch or protos.tag must be set")
        }

        ProtosConfig(
            url = customProtosUrl,
            branchTag = branchTag
        )
    } else {
        ProtosConfig(
            url = "https://github.com/protonpass/pass-contents-proto-definition.git",
            branchTag = BranchOrTag.Tag(PROTOBUF_TAG)
        )
    }
}

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupByRegex(".*google.*")
                includeGroupByRegex(".*android.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("me.proton.core.gradle-plugins.include-core-build") version "1.3.1"
}

includeCoreBuild {
    branch.set("main")
    includeBuild("gopenpgp")

    includeRepo("contents-proto-definition") {
        val config = getProtosConfig()
        uri.set(config.url)

        when (val value = config.branchTag) {
            is BranchOrTag.Branch -> branch.set(value.name)
            is BranchOrTag.Tag -> tag.set(value.name)
        }

        checkoutDirectory.set(file("./pass/protos/contents-proto-definition"))
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":app")
include(":appmacrobenchmark")
include(":pass:account:api")
include(":pass:account:fakes")
include(":pass:account:impl")
include(":pass:app-config:api")
include(":pass:app-config:fakes")
include(":pass:autofill:api")
include(":pass:autofill:e2e-app")
include(":pass:autofill:fakes")
include(":pass:autofill:impl")
include(":pass:autofill:microbenchmark")
include(":pass:autofill:test-app")
include(":pass:biometry:api")
include(":pass:biometry:fakes")
include(":pass:biometry:impl")
include(":pass:clipboard:api")
include(":pass:clipboard:fakes")
include(":pass:clipboard:impl")
include(":pass:common-presentation:api")
include(":pass:common-presentation:fakes")
include(":pass:common-presentation:impl")
include(":pass:common-rust:api")
include(":pass:common-rust:fakes")
include(":pass:common-rust:impl")
include(":pass:common-test")
include(":pass:common-ui-models:api")
include(":pass:common-ui-models:fakes")
include(":pass:common-ui:api")
include(":pass:common-ui:fakes")
include(":pass:common-ui:impl")
include(":pass:common:api")
include(":pass:common:fakes")
include(":pass:common:impl")
include(":pass:compose-components:impl")
include(":pass:crypto:api")
include(":pass:crypto:fakes")
include(":pass:crypto:impl")
include(":pass:data-models:api")
include(":pass:data:api")
include(":pass:data:fakes")
include(":pass:data:impl")
include(":pass:domain")
include(":pass:feature-feature-flags:impl")
include(":pass:feature-home:demo-app")
include(":pass:feature-home:impl")
include(":pass:feature-home:macrobenchmark")
include(":pass:feature-item-create:impl")
include(":pass:feature-item-detail:impl")
include(":pass:feature-migrate:impl")
include(":pass:feature-onboarding:impl")
include(":pass:feature-profile:impl")
include(":pass:features:account")
include(":pass:features:account-selector")
include(":pass:features:alias-contacts")
include(":pass:features:attachments")
include(":pass:features:auth")
include(":pass:features:extrapassword")
include(":pass:features:in-app-messages")
include(":pass:features:item-details")
include(":pass:features:item-history")
include(":pass:features:item-options")
include(":pass:features:item-trash")
include(":pass:features:passkeys")
include(":pass:features:password")
include(":pass:features:report")
include(":pass:features:search-options")
include(":pass:features:secure-links")
include(":pass:features:security-center")
include(":pass:features:select-item")
include(":pass:features:settings")
include(":pass:features:sharing")
include(":pass:features:sl-sync")
include(":pass:features:sync")
include(":pass:features:trash")
include(":pass:features:trial")
include(":pass:features:upsell")
include(":pass:features:vault")
include(":pass:files:api")
include(":pass:files:fakes")
include(":pass:files:impl")
include(":pass:image:api")
include(":pass:image:fakes")
include(":pass:image:impl")
include(":pass:in-app-review:api")
include(":pass:in-app-review:fakes")
include(":pass:in-app-review:fdroid")
include(":pass:in-app-review:impl")
include(":pass:in-app-updates:api")
include(":pass:in-app-updates:fdroid")
include(":pass:in-app-updates:impl")
include(":pass:log:api")
include(":pass:log:fakes")
include(":pass:log:impl")
include(":pass:navigation:api")
include(":pass:network:api")
include(":pass:network:fakes")
include(":pass:network:impl")
include(":pass:notifications:api")
include(":pass:notifications:fakes")
include(":pass:notifications:impl")
include(":pass:passkeys:api")
include(":pass:passkeys:fakes")
include(":pass:passkeys:impl")
include(":pass:preferences:api")
include(":pass:preferences:fakes")
include(":pass:preferences:impl")
include(":pass:protos")
include(":pass:screenshot-tests")
include(":pass:search-options:api")
include(":pass:search-options:fakes")
include(":pass:search-options:impl")
include(":pass:security-center:api")
include(":pass:security-center:fakes")
include(":pass:security-center:impl")
include(":pass:security-center:microbenchmark")
include(":pass:telemetry:api")
include(":pass:telemetry:fakes")
include(":pass:telemetry:impl")
include(":pass:totp:api")
include(":pass:totp:fakes")
include(":pass:totp:impl")
include(":pass:tracing:fdroid")
include(":pass:tracing:impl")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google {
            mavenContent {
                includeGroupByRegex(".*google.*")
                includeGroupByRegex(".*android.*")
            }
        }
        mavenCentral()
    }
}

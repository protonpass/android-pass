/*
 * Copyright (c) 2026 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

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

val PROTOBUF_TAG = "1.6.0"

val isCI = System.getenv("GITLAB_CI").toBoolean()

fun getProtosConfig(): ProtosConfig {
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
    // branch.set("main")

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

buildCache {
    local {
        val cacheDir = if (isCI) {
            File(rootDir, ".gradle/build-cache")
        } else {
            File(rootDir.parentFile, "protonpass-build-cache")
        }
        directory = cacheDir
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

// Pass Common local composite build
// To use local sources add to local.properties (git-ignored):
//   pass.common.local.path=/Users/<you>/Development/proton-pass-common/proton-pass-mobile/android
// Or set env var: PASS_COMMON_LOCAL_PATH
// NOTE: pre-built .so files in lib/src/main/jniLibs/ are used automatically.
//       Re-run `make android-lib-aarch64 android-lib-armv7 android-lib-x86_64`
//       only when you change Rust code in proton-pass-common.
val passCommonLocalPath = localProperties.getProperty("pass.common.local.path")
    ?: System.getenv("PASS_COMMON_LOCAL_PATH")
val passCommonLocalDir = passCommonLocalPath?.let { File(it) }
if (passCommonLocalDir?.isDirectory == true) {
    logger.lifecycle("pass-common: using LOCAL composite build from $passCommonLocalDir")
    includeBuild(passCommonLocalDir) {
        dependencySubstitution {
            substitute(module("me.proton.pass.common:lib")).using(project(":lib"))
        }
    }
}

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
include(":pass:features:account")
include(":pass:features:account-selector")
include(":pass:features:alias-contacts")
include(":pass:features:attachments")
include(":pass:features:auth")
include(":pass:features:credentials")
include(":pass:features:extrapassword")
include(":pass:features:feature-flags")
include(":pass:features:home")
include(":pass:features:home-benchmark")
include(":pass:features:home-demo")
include(":pass:features:in-app-messages")
include(":pass:features:item-create")
include(":pass:features:item-details")
include(":pass:features:item-history")
include(":pass:features:item-options")
include(":pass:features:item-trash")
include(":pass:features:migrate")
include(":pass:features:onboarding")
include(":pass:features:passkeys")
include(":pass:features:password")
include(":pass:features:profile")
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
include(":pass:in-app-review:no-op")
include(":pass:in-app-review:impl")
include(":pass:in-app-updates:api")
include(":pass:in-app-updates:no-op")
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
include(":pass:tracing:no-op")
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

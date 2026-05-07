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

/**
 * Gradle init script that inserts the Proton internal Maven cache proxy as the
 * first entry in every repository container. Standard repos declared in build and
 * settings scripts (google(), mavenCentral(), gradlePluginPortal(), …) are kept
 * as-is and act as fallbacks when an artifact is not found in the proxy.
 *
 * Requires the MAVEN_CACHE_PKG environment variable to be set to the proxy URL.
 * When the variable is absent the script is a no-op, so local builds are unaffected.
 *
 * Deploy to CI runners by copying this file to $GRADLE_USER_HOME/init.d/, or pass
 * it explicitly to every Gradle invocation:
 *   ./gradlew -I gradle/proton-maven-cache.init.gradle.kts <tasks>
 *
 * The beforeSettings / beforeProject hooks fire before any build or settings script
 * runs, so the proxy is added to an empty container and user-declared repos slot in
 * naturally after it — no reordering required. This also covers repositories declared
 * inside included builds (e.g. build-logic) and buildSrc.
 */
apply<ProtonMavenCacheInitPlugin>()

class ProtonMavenCacheInitPlugin : Plugin<Gradle> {

    override fun apply(gradle: Gradle) {
        fun RepositoryHandler.addRepos() {
            val protonMavenUrl = System.getenv("MAVEN_CACHE_PKG")
            if (protonMavenUrl != null) {
                maven {
                    setUrl(protonMavenUrl)
                    // content {
                    //     excludeModule("com.squareup", "javapoet")
                    // }
                }
            }
            mavenCentral()
            google()
            gradlePluginPortal()
        }

        gradle.beforeSettings {
            pluginManagement.repositories.addRepos()
            dependencyResolutionManagement.repositories.addRepos()
        }
    }
}

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

package proton.android.pass.data.impl.usecases

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test
import proton.android.pass.appconfig.api.AppConfig
import proton.android.pass.appconfig.api.BuildEnv
import proton.android.pass.appconfig.api.BuildFlavor
import proton.android.pass.preferences.FakeFeatureFlagsPreferenceRepository
import proton.android.pass.preferences.FeatureFlag

class ObserveShouldShowExploreTabImplTest {

    @Test
    fun `emits true when FF on and flavor is Play`() = runTest {
        val ffRepo = FakeFeatureFlagsPreferenceRepository()
        ffRepo.set<Boolean>(FeatureFlag.PASS_EXPLORE_TAB, true)

        val useCase = ObserveShouldShowExploreTabImpl(
            featureFlagsRepository = ffRepo,
            appConfig = appConfigWithFlavor(BuildFlavor.Play(BuildEnv.PROD))
        )

        useCase().test {
            assertThat(awaitItem()).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits false when FF on but flavor is Quest`() = runTest {
        val ffRepo = FakeFeatureFlagsPreferenceRepository()
        ffRepo.set<Boolean>(FeatureFlag.PASS_EXPLORE_TAB, true)

        val useCase = ObserveShouldShowExploreTabImpl(
            featureFlagsRepository = ffRepo,
            appConfig = appConfigWithFlavor(BuildFlavor.Quest(BuildEnv.PROD))
        )

        useCase().test {
            assertThat(awaitItem()).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits false when FF off`() = runTest {
        val ffRepo = FakeFeatureFlagsPreferenceRepository()

        val useCase = ObserveShouldShowExploreTabImpl(
            featureFlagsRepository = ffRepo,
            appConfig = appConfigWithFlavor(BuildFlavor.Play(BuildEnv.PROD))
        )

        useCase().test {
            assertThat(awaitItem()).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun appConfigWithFlavor(buildFlavor: BuildFlavor): AppConfig = object : AppConfig {
        override val isDebug: Boolean = false
        override val applicationId: String = ""
        override val flavor: BuildFlavor = buildFlavor
        override val versionCode: Int = 0
        override val versionName: String = "0.0.0"
        override val host: String = ""
        override val humanVerificationHost: String = ""
        override val proxyToken: String? = null
        override val useDefaultPins: Boolean = true
        override val sentryDSN: String? = null
        override val accountSentryDSN: String? = null
        override val androidVersion: Int = 0
        override val allowScreenshotsDefaultValue: Boolean = true
    }
}

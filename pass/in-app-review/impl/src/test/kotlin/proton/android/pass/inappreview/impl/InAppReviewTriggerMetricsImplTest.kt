/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.inappreview.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.Before
import org.junit.Test
import proton.android.pass.preferences.AppUsageConfig
import proton.android.pass.preferences.FakeInternalSettingsRepository
import proton.android.pass.test.FixedClock

class InAppReviewTriggerMetricsImplTest {

    private lateinit var internalSettingsRepository: FakeInternalSettingsRepository
    private lateinit var clock: Clock
    private lateinit var inAppReviewTriggerMetrics: InAppReviewTriggerMetricsImpl

    @Before
    fun setUp() {
        internalSettingsRepository = FakeInternalSettingsRepository()
        clock = FixedClock(Instant.parse("2023-08-01T00:00:00Z"))
        inAppReviewTriggerMetrics = InAppReviewTriggerMetricsImpl(
            internalSettingsRepository = internalSettingsRepository,
            clock = clock
        )
    }

    @Test
    fun `should increment item count`() = runTest {
        inAppReviewTriggerMetrics.incrementItemCreatedCount()
        internalSettingsRepository.getItemCreateCount().test {
            assertThat(awaitItem()).isEqualTo(1)
        }
    }

    @Test
    fun `should increment autofill count`() = runTest {
        inAppReviewTriggerMetrics.incrementItemAutofillCount()
        internalSettingsRepository.getItemAutofillCount().test {
            assertThat(awaitItem()).isEqualTo(1)
        }
    }

    @Test
    fun `should increment app launch streak count when lastDateUsed is null`() = runTest {

        inAppReviewTriggerMetrics.incrementAppLaunchStreakCount()

        internalSettingsRepository.getAppUsage().test {
            val item = awaitItem()
            assertThat(item.timesUsed).isEqualTo(1)
            assertThat(item.lastDateUsed).isEqualTo(clock.now())
        }
    }

    @Test
    fun `should not increment app launch streak count when lastDateUsed is the same day as current time`() = runTest {
        internalSettingsRepository.setAppUsage(
            AppUsageConfig(1, Instant.parse("2023-08-01T00:00:00Z"))
        )

        inAppReviewTriggerMetrics.incrementAppLaunchStreakCount()

        internalSettingsRepository.getAppUsage().test {
            val item = awaitItem()
            assertThat(item.timesUsed).isEqualTo(1)
            assertThat(item.lastDateUsed).isEqualTo(clock.now())
        }
    }

    @Test
    fun `should reset app launch streak count when lastDateUsed is the previous day of current time`() = runTest {
        internalSettingsRepository.setAppUsage(
            AppUsageConfig(2, Instant.parse("2023-07-30T00:00:00Z"))
        )

        inAppReviewTriggerMetrics.incrementAppLaunchStreakCount()

        internalSettingsRepository.getAppUsage().test {
            val item = awaitItem()
            assertThat(item.timesUsed).isEqualTo(1)
            assertThat(item.lastDateUsed).isEqualTo(clock.now())
        }
    }

    @Test
    fun `should increment app launch streak count on date is yesterday`() = runTest {
        internalSettingsRepository.setAppUsage(
            AppUsageConfig(2, Instant.parse("2023-07-31T00:00:00Z"))
        )

        inAppReviewTriggerMetrics.incrementAppLaunchStreakCount()

        internalSettingsRepository.getAppUsage().test {
            val item = awaitItem()
            assertThat(item.timesUsed).isEqualTo(3)
            assertThat(item.lastDateUsed).isEqualTo(clock.now())
        }
    }
}

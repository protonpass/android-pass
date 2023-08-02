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

import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import proton.android.pass.inappreview.api.InAppReviewTriggerMetrics
import proton.android.pass.preferences.AppUsageConfig
import proton.android.pass.preferences.InternalSettingsRepository
import javax.inject.Inject

class InAppReviewTriggerMetricsImpl @Inject constructor(
    private val internalSettingsRepository: InternalSettingsRepository,
    private val clock: Clock
) : InAppReviewTriggerMetrics {

    override suspend fun incrementItemCreatedCount() {
        val count: Int = internalSettingsRepository.getItemCreateCount().firstOrNull() ?: 0
        internalSettingsRepository.setItemCreateCount(count.inc())
    }

    override suspend fun incrementItemAutofillCount() {
        val count: Int = internalSettingsRepository.getItemAutofillCount().firstOrNull() ?: 0
        internalSettingsRepository.setItemAutofillCount(count.inc())
    }

    override suspend fun incrementAppLaunchStreakCount() {
        val now = clock.now()
        val nowAsLocal = now.toLocalDateTime(TimeZone.UTC)
        val appUsageConfig = internalSettingsRepository.getAppUsage().firstOrNull()
            ?: AppUsageConfig(0, now)
        val lastDateUsedAsLocal = appUsageConfig.lastDateUsed.toLocalDateTime(TimeZone.UTC)
        val nowMinusOneDay = now.minus(24, DateTimeUnit.HOUR).toLocalDateTime(TimeZone.UTC)
        when (lastDateUsedAsLocal.date) {
            nowAsLocal.date -> {}
            nowMinusOneDay.date -> internalSettingsRepository.setAppUsage(
                appUsageConfig.copy(
                    timesUsed = appUsageConfig.timesUsed.inc(),
                    lastDateUsed = now
                )
            )
            else -> internalSettingsRepository.setAppUsage(
                appUsageConfig.copy(
                    timesUsed = 1,
                    lastDateUsed = now
                )
            )
        }
    }
}

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

package proton.android.pass.data.impl.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull
import proton.android.pass.data.api.ItemCountSummary
import proton.android.pass.data.api.usecases.ObserveItemCount
import proton.android.pass.domain.ShareSelection
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.DisplayFileAttachmentsBanner
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.featurediscovery.FeatureDiscoveryBannerPreference
import proton.android.pass.preferences.featurediscovery.FeatureDiscoveryFeature
import java.util.concurrent.TimeUnit

@HiltWorker
class PeriodicFeatureDiscoveryWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted private val workerParameters: WorkerParameters,
    private val observeItemCount: ObserveItemCount,
    private val userPreferencesRepository: UserPreferencesRepository
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result = runCatching {
        PassLogger.i(TAG, "Starting $TAG attempt $runAttemptCount")
        val summary = observeItemCount(
            shareSelection = ShareSelection.AllShares,
            includeHiddenVault = true
        ).firstOrNull()
            ?: ItemCountSummary.Initial
        processFileAttachmentsBannerDisplayLogic(summary.total)
        processAliasManagementBannerDisplayLogic(summary.alias)
    }.onSuccess {
        PassLogger.i(TAG, "Finished $TAG")
    }.onFailure {
        PassLogger.w(TAG, "Failed to run $TAG")
        PassLogger.w(TAG, it)
    }.toWorkerResult()

    private suspend fun processFileAttachmentsBannerDisplayLogic(total: Long) {
        val preference = userPreferencesRepository.observeDisplayFileAttachmentsOnboarding()
            .firstOrNull()
            ?: DisplayFileAttachmentsBanner.NotDisplay
        if (preference == DisplayFileAttachmentsBanner.Unknown && total > FILE_ATTACHMENTS_ITEM_AMOUNT_THRESHOLD) {
            userPreferencesRepository.setDisplayFileAttachmentsOnboarding(
                DisplayFileAttachmentsBanner.Display
            )
        }
    }

    private suspend fun processAliasManagementBannerDisplayLogic(alias: Long) {
        if (alias <= ALIAS_MANAGEMENT_ALIAS_AMOUNT_THRESHOLD) return

        checkAndSetBannerDisplay(FeatureDiscoveryFeature.AliasManagementContacts)
        checkAndSetBannerDisplay(FeatureDiscoveryFeature.AliasManagementMailbox)
        checkAndSetBannerDisplay(FeatureDiscoveryFeature.AliasManagementCustomDomain)
        checkAndSetBannerDisplay(FeatureDiscoveryFeature.AliasManagementOptions)
    }

    private suspend fun checkAndSetBannerDisplay(feature: FeatureDiscoveryFeature) {
        val bannerPreference = userPreferencesRepository.observeDisplayFeatureDiscoverBanner(feature)
            .firstOrNull()
            ?: FeatureDiscoveryBannerPreference.NotDisplay

        if (bannerPreference == FeatureDiscoveryBannerPreference.Unknown) {
            userPreferencesRepository.setDisplayFeatureDiscoverBanner(
                feature,
                FeatureDiscoveryBannerPreference.Display
            )
        }
    }

    companion object {
        const val WORKER_UNIQUE_NAME = "periodic_feature_discovery_worker"
        private const val TAG = "PeriodicFeatureDiscoveryWorker"
        private const val REPEAT_DAYS = 1L
        private const val INITIAL_DELAY_DAYS = 1L
        private const val FILE_ATTACHMENTS_ITEM_AMOUNT_THRESHOLD = 10
        private const val ALIAS_MANAGEMENT_ALIAS_AMOUNT_THRESHOLD = 2

        fun getRequestFor(): PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<PeriodicFeatureDiscoveryWorker>(REPEAT_DAYS, TimeUnit.DAYS)
                .setInitialDelay(INITIAL_DELAY_DAYS, TimeUnit.DAYS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
    }
}




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

package proton.android.pass.data.impl.usecases

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import me.proton.core.eventmanager.domain.work.EventWorkerManager
import proton.android.pass.data.api.usecases.InitialWorkerLauncher
import proton.android.pass.data.api.usecases.WorkerFeature
import proton.android.pass.data.impl.work.PeriodicAssetLinkWorker
import proton.android.pass.data.impl.work.PeriodicCleanupWorker
import proton.android.pass.data.impl.work.PeriodicFeatureDiscoveryWorker
import proton.android.pass.data.impl.work.PeriodicIgnoredAssetLinkWorker
import proton.android.pass.data.impl.work.PeriodicReportWorker
import proton.android.pass.data.impl.work.UserAccessWorker
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class InitialWorkerLauncherImpl @Inject constructor(
    private val workManager: WorkManager,
    private val eventWorkerManager: EventWorkerManager,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val featureFlagsPreferencesRepository: FeatureFlagsPreferencesRepository
) : InitialWorkerLauncher {

    private val featureLaunchActions: Map<WorkerFeature, () -> Unit> = mapOf(
        WorkerFeature.USER_ACCESS to ::launchUserAccessWorker,
        WorkerFeature.CLEANUP to ::launchCleanupWorker,
        WorkerFeature.FEATURE_DISCOVERY to ::launchFeatureDiscoveryWorker,
        WorkerFeature.REPORT to ::launchReportWorker,
        WorkerFeature.ASSET_LINKS to ::launchAssetLinkWorkers
    )

    private val featureToWorkersMap: Map<WorkerFeature, List<String>> = mapOf(
        WorkerFeature.USER_ACCESS to listOf(UserAccessWorker.WORKER_UNIQUE_NAME),
        WorkerFeature.CLEANUP to listOf(PeriodicCleanupWorker.WORKER_UNIQUE_NAME),
        WorkerFeature.FEATURE_DISCOVERY to listOf(PeriodicFeatureDiscoveryWorker.WORKER_UNIQUE_NAME),
        WorkerFeature.REPORT to listOf(PeriodicReportWorker.WORKER_UNIQUE_NAME),
        WorkerFeature.ASSET_LINKS to listOf(
            PeriodicAssetLinkWorker.WORKER_UNIQUE_NAME,
            PeriodicIgnoredAssetLinkWorker.WORKER_UNIQUE_NAME
        )
    )

    override fun start() {
        PassLogger.i(TAG, "Starting periodic workers")
        launchFeature(WorkerFeature.USER_ACCESS)
        launchFeature(WorkerFeature.CLEANUP)
        launchFeature(WorkerFeature.FEATURE_DISCOVERY)
        launchFeature(WorkerFeature.REPORT)

        if (isDALEnabled()) {
            launchFeature(WorkerFeature.ASSET_LINKS)
        } else {
            cancelFeature(WorkerFeature.ASSET_LINKS)
        }
    }

    private fun isDALEnabled() = runBlocking {
        val isFeatureFlagEnabled = featureFlagsPreferencesRepository
            .get<Boolean>(FeatureFlag.DIGITAL_ASSET_LINKS)
            .firstOrNull()
            ?: false

        val isUserPreferenceEnabled = userPreferencesRepository
            .observeUseDigitalAssetLinksPreference()
            .firstOrNull()
            ?.value()
            ?: false

        isFeatureFlagEnabled && isUserPreferenceEnabled
    }

    private fun launchFeature(feature: WorkerFeature) {
        val action = featureLaunchActions[feature]
        if (action != null) {
            action()
        } else {
            throw IllegalStateException("No action found for feature: $feature")
        }
    }

    private fun launchUserAccessWorker() {
        val backoffDelaySeconds = eventWorkerManager.getBackoffDelay().inWholeSeconds
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val workRequest: PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<UserAccessWorker>(1, TimeUnit.DAYS)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    backoffDelaySeconds,
                    TimeUnit.SECONDS
                )
                .setConstraints(constraints)
                .build()
        workManager.enqueueUniquePeriodicWork(
            UserAccessWorker.WORKER_UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun launchCleanupWorker() {
        workManager.enqueueUniquePeriodicWork(
            PeriodicCleanupWorker.WORKER_UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicCleanupWorker.getRequestFor()
        )
    }

    private fun launchFeatureDiscoveryWorker() {
        workManager.enqueueUniquePeriodicWork(
            PeriodicFeatureDiscoveryWorker.WORKER_UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicFeatureDiscoveryWorker.getRequestFor()
        )
    }

    private fun launchReportWorker() {
        workManager.enqueueUniquePeriodicWork(
            PeriodicReportWorker.WORKER_UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicReportWorker.getRequestFor()
        )
    }

    private fun launchAssetLinkWorkers() {
        workManager.enqueueUniquePeriodicWork(
            PeriodicAssetLinkWorker.WORKER_UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicAssetLinkWorker.getRequestFor()
        )
        workManager.enqueueUniquePeriodicWork(
            PeriodicIgnoredAssetLinkWorker.WORKER_UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicIgnoredAssetLinkWorker.getRequestFor()
        )
    }

    override fun cancel() {
        PassLogger.i(TAG, "Cancelling periodic workers")
        WorkerFeature.entries.forEach(::cancelFeature)
    }

    override fun cancelFeature(feature: WorkerFeature) {
        PassLogger.i(TAG, "Cancelling worker ${feature.name}")
        val workerNames = featureToWorkersMap[feature]
        if (workerNames != null) {
            workerNames.forEach { workerName ->
                workManager.cancelUniqueWork(workerName)
            }
        } else {
            throw IllegalStateException("No workers found for feature: $feature")
        }
    }

    companion object {
        private const val TAG = "UserPlanWorkerLauncherImpl"
    }
}

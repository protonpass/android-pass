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

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.data.api.usecases.InitialWorkerLauncher
import proton.android.pass.data.api.usecases.WorkerFeature
import proton.android.pass.data.impl.work.ClearPasswordHistoryWorker
import proton.android.pass.data.impl.work.PeriodicAssetLinkWorker
import proton.android.pass.data.impl.work.PeriodicCleanupWorker
import proton.android.pass.data.impl.work.PeriodicFeatureDiscoveryWorker
import proton.android.pass.data.impl.work.PeriodicIgnoredAssetLinkWorker
import proton.android.pass.data.impl.work.PeriodicReportWorker
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import javax.inject.Inject

class InitialWorkerLauncherImpl @Inject constructor(
    private val workManager: WorkManager,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val appDispatchers: AppDispatchers
) : InitialWorkerLauncher {

    private val featureLaunchActions: Map<WorkerFeature, () -> Unit> = mapOf(
        WorkerFeature.CLEANUP to ::launchCleanupWorker,
        WorkerFeature.FEATURE_DISCOVERY to ::launchFeatureDiscoveryWorker,
        WorkerFeature.REPORT to ::launchReportWorker,
        WorkerFeature.ASSET_LINKS to ::launchAssetLinkWorkers,
        WorkerFeature.PASSWORD_HISTORY to ::launchPasswordHistoryWorker
    )

    private val featureToWorkersMap: Map<WorkerFeature, List<String>> = mapOf(
        WorkerFeature.CLEANUP to listOf(PeriodicCleanupWorker.WORKER_UNIQUE_NAME),
        WorkerFeature.FEATURE_DISCOVERY to listOf(PeriodicFeatureDiscoveryWorker.WORKER_UNIQUE_NAME),
        WorkerFeature.REPORT to listOf(PeriodicReportWorker.WORKER_UNIQUE_NAME),
        WorkerFeature.ASSET_LINKS to listOf(
            PeriodicAssetLinkWorker.WORKER_UNIQUE_NAME,
            PeriodicIgnoredAssetLinkWorker.WORKER_UNIQUE_NAME
        ),
        WorkerFeature.PASSWORD_HISTORY to listOf(ClearPasswordHistoryWorker.WORKER_UNIQUE_NAME)
    )

    override fun start() {
        PassLogger.i(TAG, "Starting periodic workers")
        launchFeature(WorkerFeature.CLEANUP)
        launchFeature(WorkerFeature.FEATURE_DISCOVERY)
        launchFeature(WorkerFeature.REPORT)
        launchFeature(WorkerFeature.PASSWORD_HISTORY)

        CoroutineScope(appDispatchers.io).launch {
            if (isDALEnabled()) {
                launchFeature(WorkerFeature.ASSET_LINKS)
            } else {
                cancelFeature(WorkerFeature.ASSET_LINKS)
            }
        }
    }

    private suspend fun isDALEnabled(): Boolean = userPreferencesRepository
        .observeUseDigitalAssetLinksPreference()
        .firstOrNull()
        ?.value()
        ?: false

    private fun launchFeature(feature: WorkerFeature) {
        val action = featureLaunchActions[feature]
        if (action != null) {
            action()
        } else {
            throw IllegalStateException("No action found for feature: $feature")
        }
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

    private fun launchPasswordHistoryWorker() {
        workManager.enqueueUniquePeriodicWork(
            ClearPasswordHistoryWorker.WORKER_UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            ClearPasswordHistoryWorker.getRequestFor()
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

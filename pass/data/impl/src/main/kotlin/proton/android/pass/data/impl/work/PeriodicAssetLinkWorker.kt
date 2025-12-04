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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit.Companion.DAY
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getAccounts
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.data.api.repositories.AssetLinkRepository
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveItems
import proton.android.pass.data.impl.usecases.assetlink.UpdateAssetLink
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.ShareSelection
import proton.android.pass.log.api.PassLogger
import java.util.concurrent.TimeUnit

@HiltWorker
class PeriodicAssetLinkWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted private val workerParameters: WorkerParameters,
    private val accountManager: AccountManager,
    private val observeItems: ObserveItems,
    private val assetLinkRepository: AssetLinkRepository,
    private val updateAssetLink: UpdateAssetLink,
    private val clock: Clock
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result = safeRunCatching {
        PassLogger.i(TAG, "Starting $TAG attempt $runAttemptCount")
        purgeOldData()
        val websites: Set<String> = getAllWebsites()
        updateAssetLink(websites)
    }.onSuccess {
        PassLogger.i(TAG, "Finished $TAG")
    }.onFailure {
        PassLogger.w(TAG, "Failed update asset links")
        PassLogger.w(TAG, it)
    }.toWorkerResult()

    private suspend fun getAllWebsites() = accountManager.getAccounts(AccountState.Ready)
        .flatMapLatest { list ->
            val flows = list.map { user ->
                observeItems(
                    selection = ShareSelection.AllShares,
                    itemState = ItemState.Active,
                    filter = ItemTypeFilter.Logins,
                    userId = user.userId,
                    includeHidden = false
                )
            }
            combine(flows) { it.toList().flatten() }
        }
        .mapLatest { list ->
            list.flatMap { (it.itemType as ItemType.Login).websites }.toSet()
        }
        .first()

    private suspend fun purgeOldData() {
        safeRunCatching {
            val date14DaysAgo: Instant = clock.now().minus(
                REPEAT_DAYS, DAY, TimeZone.currentSystemDefault()
            )
            assetLinkRepository.purgeOlderThan(date14DaysAgo)
        }.onSuccess {
            PassLogger.i(TAG, "Purged old asset links")
        }.onFailure {
            PassLogger.w(TAG, "Failed to purge old asset links")
            PassLogger.w(TAG, it)
        }
    }

    companion object {
        const val WORKER_UNIQUE_NAME = "periodic_asset_link_worker"
        private const val TAG = "PeriodicAssetLinkWorker"
        private const val REPEAT_DAYS = 14L

        fun getRequestFor(): PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<PeriodicAssetLinkWorker>(REPEAT_DAYS, TimeUnit.DAYS)
                .setInitialDelay(1, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                )
                .build()
    }
}




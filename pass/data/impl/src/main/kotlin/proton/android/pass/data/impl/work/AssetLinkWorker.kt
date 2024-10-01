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
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getAccounts
import proton.android.pass.commonrust.api.DomainManager
import proton.android.pass.data.api.repositories.AssetLinkRepository
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveItems
import proton.android.pass.data.impl.util.runConcurrently
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.ShareSelection
import proton.android.pass.domain.assetlink.AssetLink
import proton.android.pass.log.api.PassLogger
import java.util.concurrent.TimeUnit

@HiltWorker
class AssetLinkWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted private val workerParameters: WorkerParameters,
    private val accountManager: AccountManager,
    private val observeItems: ObserveItems,
    private val assetLinkRepository: AssetLinkRepository,
    private val domainManager: DomainManager
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result = runCatching {
        PassLogger.i(TAG, "Starting $TAG attempt $runAttemptCount")
        val websites: Set<String> = accountManager.getAccounts(AccountState.Ready)
            .flatMapLatest { list ->
                val flows = list.map { user ->
                    observeItems(
                        selection = ShareSelection.AllShares,
                        itemState = ItemState.Active,
                        filter = ItemTypeFilter.Logins,
                        userId = user.userId
                    )
                }
                combine(flows) { it.toList().flatten() }
            }
            .mapLatest { list ->
                list.flatMap { (it.itemType as ItemType.Login).websites }
                    .map(domainManager::getRoot)
                    .toSet()
            }
            .first()
        val results: List<kotlin.Result<AssetLink>> = runConcurrently(
            items = websites,
            block = assetLinkRepository::fetch
        )
        val (successes, failures) = results.partition { it.isSuccess }
        if (failures.isNotEmpty()) {
            PassLogger.w(
                TAG,
                "${failures.size} from ${results.size} websites failed to get asset links"
            )
        }
        val assetLinks = successes.mapNotNull { it.getOrNull() }
        assetLinkRepository.insert(assetLinks)
    }
        .onFailure {
            PassLogger.w(TAG, "Failed to get websites")
            PassLogger.w(TAG, it)
        }
        .toWorkerResult()

    companion object {
        const val WORKER_UNIQUE_NAME = "asset_link_worker"
        private const val TAG = "AssetLinkWorker"
        private const val REPEAT_DAYS = 14L

        fun getRequestFor(): PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<AssetLinkWorker>(REPEAT_DAYS, TimeUnit.DAYS)
                .setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                )
                .build()
    }
}




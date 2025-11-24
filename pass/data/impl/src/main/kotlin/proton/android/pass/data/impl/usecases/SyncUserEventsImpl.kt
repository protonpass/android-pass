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

import androidx.lifecycle.asFlow
import androidx.work.WorkManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.PromoteNewInviteToInvite
import proton.android.pass.data.api.usecases.RefreshInvites
import proton.android.pass.data.api.usecases.RefreshPlan
import proton.android.pass.data.api.usecases.RefreshSharesAndEnqueueSync
import proton.android.pass.data.api.usecases.RefreshSharesResult
import proton.android.pass.data.api.usecases.SyncUserEvents
import proton.android.pass.data.api.usecases.simplelogin.SyncSimpleLoginPendingAliases
import proton.android.pass.data.impl.repositories.UserEventRepository
import proton.android.pass.data.impl.work.FetchItemsWorker
import proton.android.pass.domain.UserEventId
import proton.android.pass.domain.events.SyncEventInvitesChanged
import proton.android.pass.domain.events.SyncEventShare
import proton.android.pass.domain.events.SyncEventShareItem
import proton.android.pass.domain.events.UserEventList
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class SyncUserEventsImpl @Inject constructor(
    private val userEventRepository: UserEventRepository,
    private val shareRepository: ShareRepository,
    private val itemRepository: ItemRepository,
    private val refreshSharesAndEnqueueSync: RefreshSharesAndEnqueueSync,
    private val workManager: WorkManager,
    private val refreshPlan: RefreshPlan,
    private val refreshInvites: RefreshInvites,
    private val syncPendingAliases: SyncSimpleLoginPendingAliases,
    private val promoteNewInviteToInvite: PromoteNewInviteToInvite
) : SyncUserEvents {

    override suspend fun invoke(userId: UserId) {
        PassLogger.d(TAG, "Syncing user events for $userId started")

        val localEventId = getLocalEventId(userId)
        val remoteLatestEventId = userEventRepository.fetchLatestEventId(userId)

        if (localEventId == remoteLatestEventId) {
            PassLogger.d(TAG, "Local user events already up to date for $userId")
            return
        }

        processUserEvents(userId, localEventId ?: remoteLatestEventId)

        PassLogger.i(TAG, "Syncing user events for $userId finished")
    }

    private suspend fun getLocalEventId(userId: UserId): UserEventId? {
        val localEventId = userEventRepository.getLatestEventId(userId).first()
        if (localEventId == null) fullRefresh(userId)
        return localEventId
    }

    private suspend fun processUserEvents(userId: UserId, initialEventId: UserEventId) {
        var currentEventId = initialEventId

        do {
            val eventList = userEventRepository.getUserEvents(userId, currentEventId)
            if (eventList.fullRefresh) {
                fullRefresh(userId)
            } else {
                processIncrementalEvents(userId, eventList)
            }

            userEventRepository.storeLatestEventId(userId, eventList.lastEventId)
            PassLogger.i(TAG, "Fetched user events, eventsPending: ${eventList.eventsPending}")
            currentEventId = eventList.lastEventId
        } while (eventList.eventsPending)
    }

    private suspend fun processIncrementalEvents(userId: UserId, eventList: UserEventList) {
        PassLogger.i(TAG, "Processing events for $userId")

        if (eventList.planChanged) {
            refreshPlan(userId)
        }

        processSharesCreated(userId, eventList.sharesCreated)
        processSharesUpdated(userId, eventList.sharesUpdated)
        processSharesDeleted(userId, eventList.sharesDeleted)
        processItemsUpdated(userId, eventList.itemsUpdated)
        processItemsDeleted(userId, eventList.itemsDeleted)
        processInvitesChanged(userId, eventList.invitesChanged)
        processGroupInvitesChanged(userId, eventList.groupInvitesChanged)
        processPendingAliasToCreateChanged(userId, eventList.pendingAliasToCreateChanged)
        processNewUserInvitesChanged(userId, eventList.sharesWithInvitesToCreate)
    }

    private suspend fun processSharesCreated(userId: UserId, sharesCreated: List<SyncEventShare>) {
        sharesCreated.forEach { (shareId, token) ->
            shareRepository.recreateShare(userId, shareId, token)
        }
    }

    private suspend fun processSharesUpdated(userId: UserId, sharesUpdated: List<SyncEventShare>) {
        sharesUpdated.forEach { (shareId, token) ->
            shareRepository.refreshShare(userId, shareId, token)
        }
    }

    private suspend fun processItemsUpdated(userId: UserId, itemsUpdated: List<SyncEventShareItem>) {
        itemsUpdated.forEach { (shareId, itemId, token) ->
            itemRepository.refreshItem(userId, shareId, itemId, token)
        }
    }

    private suspend fun processSharesDeleted(userId: UserId, sharesDeleted: List<SyncEventShare>) {
        if (sharesDeleted.isNotEmpty()) {
            val sharesToDelete = sharesDeleted.map(SyncEventShare::shareId)
            shareRepository.deleteLocalShares(userId, sharesToDelete)
        }
    }

    private suspend fun processItemsDeleted(userId: UserId, itemsDeleted: List<SyncEventShareItem>) {
        if (itemsDeleted.isNotEmpty()) {
            val itemsToDelete = itemsDeleted
                .groupBy { it.shareId }
                .mapValues { values -> values.value.map(SyncEventShareItem::itemId) }
            itemRepository.deleteLocalItems(userId, itemsToDelete)
        }
    }

    private suspend fun processInvitesChanged(userId: UserId, invitesChanged: SyncEventInvitesChanged?) {
        invitesChanged?.let { refreshInvites(userId, it.eventToken) }
    }

    private fun processGroupInvitesChanged(userId: UserId, invitesChanged: SyncEventInvitesChanged?) {
        invitesChanged?.let {
            // refresh group invites
        }
    }

    private suspend fun processPendingAliasToCreateChanged(userId: UserId, invitesChanged: SyncEventInvitesChanged?) {
        invitesChanged?.let { syncPendingAliases(userId) }
    }

    private suspend fun processNewUserInvitesChanged(userId: UserId, sharesWithInvitesToCreate: List<SyncEventShare>) {
        if (sharesWithInvitesToCreate.isNotEmpty()) {
            sharesWithInvitesToCreate.forEach { (shareId, token) ->
                promoteNewInviteToInvite(userId, shareId)
            }
        }
    }

    private suspend fun fullRefresh(userId: UserId) {
        val result = refreshSharesAndEnqueueSync(
            userId = userId,
            syncType = RefreshSharesAndEnqueueSync.SyncType.FULL
        )
        when (result) {
            is RefreshSharesResult.SharesFound -> if (result.isWorkerEnqueued) {
                waitForFetchItemsWorker(userId)
            }

            RefreshSharesResult.NoSharesSkipped,
            RefreshSharesResult.NoSharesVaultCreated -> Unit
        }
    }

    private suspend fun waitForFetchItemsWorker(userId: UserId) {
        val uniqueName = FetchItemsWorker.getOneTimeUniqueWorkName(userId)
        workManager.awaitUniqueWorkFinished(uniqueName)
    }

    suspend fun WorkManager.awaitUniqueWorkFinished(name: String) {
        getWorkInfosForUniqueWorkLiveData(name)
            .asFlow()
            .mapNotNull { infos ->
                if (infos.isEmpty()) null
                else infos.first().state
            }
            .first { it.isFinished }
    }

    private companion object {
        private const val TAG = "SyncUserEventsImpl"
    }
}

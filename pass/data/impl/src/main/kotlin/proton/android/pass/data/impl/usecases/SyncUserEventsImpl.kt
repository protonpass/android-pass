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

import androidx.work.WorkManager
import androidx.work.await
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.delay
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.RefreshSharesAndEnqueueSync
import proton.android.pass.data.api.usecases.SyncUserEvents
import proton.android.pass.data.impl.repositories.UserEventRepository
import proton.android.pass.data.impl.work.FetchItemsWorker
import proton.android.pass.domain.UserEventId
import proton.android.pass.domain.events.SyncEventShare
import proton.android.pass.domain.events.SyncEventShareItem
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class SyncUserEventsImpl @Inject constructor(
    private val userEventRepository: UserEventRepository,
    private val shareRepository: ShareRepository,
    private val itemRepository: ItemRepository,
    private val refreshSharesAndEnqueueSync: RefreshSharesAndEnqueueSync,
    private val workManager: WorkManager
) : SyncUserEvents {

    override suspend fun invoke(userId: UserId) {
        PassLogger.d(TAG, "Syncing user events for $userId started")

        // Get latest event ID from local storage
        val localEventId: UserEventId? = userEventRepository.getLatestEventId(userId).first()

        if (localEventId == null) fullRefresh(userId)

        val remoteLatestEventId = userEventRepository.fetchLatestEventId(userId)

        if (localEventId == remoteLatestEventId) {
            PassLogger.d(TAG, "Local user events already up to date for $userId")
            return
        }

        var currentEventId = localEventId ?: remoteLatestEventId

        do {
            val eventList = userEventRepository.getUserEvents(userId, currentEventId)
            if (eventList.fullRefresh) {
                fullRefresh(userId)
            } else {
                PassLogger.i(TAG, "Processing events for $userId")

                if (eventList.planChanged) {
                    PassLogger.d(TAG, "Plan")
                }
                if (eventList.sharesDeleted.isNotEmpty()) {
                    val sharesToDelete = eventList.sharesDeleted.map(SyncEventShare::shareId)
                    shareRepository.deleteLocalShares(userId, sharesToDelete)
                }
                if (eventList.sharesUpdated.isNotEmpty()) {
                    eventList.sharesUpdated.forEach { (shareId, token) ->
                        shareRepository.refreshShare(userId, shareId, token)
                    }
                }
                if (eventList.itemsDeleted.isNotEmpty()) {
                    val itemsToDelete = eventList.itemsDeleted
                        .groupBy { it.shareId }
                        .mapValues { values -> values.value.map(SyncEventShareItem::itemId) }
                    itemRepository.deleteLocalItems(userId, itemsToDelete)
                }
                if (eventList.itemsUpdated.isNotEmpty()) {
                    eventList.itemsUpdated.forEach { (shareId, itemId, token) ->
                        itemRepository.refreshItem(userId, shareId, itemId, token)
                    }
                }
                if (eventList.sharesWithInvitesToCreate.isNotEmpty()) {
                    PassLogger.d(TAG, "Invites")
                }
                if (eventList.aliasNoteChanged.isNotEmpty()) {
                    PassLogger.d(TAG, "Note")
                }
                if (eventList.groupInvitesChanged != null) {
                    PassLogger.d(TAG, "Group")
                }
                if (eventList.invitesChanged != null) {
                    PassLogger.d(TAG, "Invites")
                }
            }

            userEventRepository.storeLatestEventId(userId, eventList.lastEventId)
            PassLogger.i(TAG, "Fetched user events, eventsPending: ${eventList.eventsPending}")
            currentEventId = eventList.lastEventId
        } while (eventList.eventsPending)

        PassLogger.i(TAG, "Syncing user events for $userId finished")
    }

    private suspend fun fullRefresh(userId: UserId) {
        refreshSharesAndEnqueueSync(
            userId = userId,
            syncType = RefreshSharesAndEnqueueSync.SyncType.FULL
        )
        waitForFetchItemsWorker(userId)
    }

    /**
     * Waits until the FetchItemsWorker unique work for the given user finishes.
     */
    private suspend fun waitForFetchItemsWorker(userId: UserId) {
        val uniqueName = FetchItemsWorker.getOneTimeUniqueWorkName(userId)
        while (currentCoroutineContext().isActive) {
            val states = workManager.getWorkInfosForUniqueWork(uniqueName).await().map { it.state }
            if (states.isEmpty() || states.all { it.isFinished }) return
            delay(WORK_CHECK_DELAY_MS)
        }
    }

    private companion object {
        private const val TAG = "SyncUserEventsImpl"
        private const val WORK_CHECK_DELAY_MS = 200L
    }
}

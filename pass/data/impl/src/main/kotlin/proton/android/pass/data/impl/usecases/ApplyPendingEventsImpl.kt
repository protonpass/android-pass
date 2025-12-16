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

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.extension.primary
import me.proton.core.user.domain.repository.UserAddressRepository
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.data.api.ItemPendingEvent
import proton.android.pass.data.api.PendingEventList
import proton.android.pass.data.api.errors.ShareNotAvailableError
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.repositories.ItemSyncStatusRepository
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.repositories.UpdateShareEvent
import proton.android.pass.data.api.usecases.ApplyPendingEvents
import proton.android.pass.data.api.usecases.RefreshSharesAndEnqueueSync
import proton.android.pass.data.api.usecases.RefreshSharesResult
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.extensions.toDomain
import proton.android.pass.data.impl.extensions.toPendingEvent
import proton.android.pass.data.impl.repositories.EventRepository
import proton.android.pass.data.impl.responses.EventList
import proton.android.pass.domain.ShareId
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

private const val TRANSACTION_NAME_APPLY_PENDING_EVENTS = "ApplyPendingEvents"

class ApplyPendingEventsImpl @Inject constructor(
    private val database: PassDatabase,
    private val eventRepository: EventRepository,
    private val addressRepository: UserAddressRepository,
    private val itemRepository: ItemRepository,
    private val shareRepository: ShareRepository,
    private val itemSyncStatusRepository: ItemSyncStatusRepository,
    private val refreshSharesAndEnqueueSync: RefreshSharesAndEnqueueSync
) : ApplyPendingEvents {

    override suspend fun invoke(userId: UserId, forceSync: Boolean) {
        PassLogger.i(TAG, "Applying pending events started (forceSync=$forceSync)")
        if (!forceSync && itemSyncStatusRepository.observeSyncState().first().isSyncing) {
            PassLogger.i(TAG, "Sync in progress, skipping")
            return
        }
        val syncType = if (forceSync) {
            RefreshSharesAndEnqueueSync.SyncType.FULL
        } else {
            RefreshSharesAndEnqueueSync.SyncType.INCREMENTAL
        }
        val result = refreshSharesAndEnqueueSync(
            userId = userId,
            syncType = syncType
        )
        when (result) {
            RefreshSharesResult.NoSharesVaultCreated,
            RefreshSharesResult.NoSharesSkipped -> return
            is RefreshSharesResult.SharesFound -> {
                PassLogger.i(TAG, "Received a list of shares, applying pending events")
                applyPendingEventsForShares(userId, result.shareIds)
            }
        }
    }

    private suspend fun applyPendingEventsForShares(userId: UserId, existingShareIds: Set<ShareId>) {

        val address = requireNotNull(
            addressRepository.getAddresses(userId).primary()
        )

        val eventResults = coroutineScope {
            existingShareIds.map { shareId ->
                async {
                    safeRunCatching {
                        fetchItemPendingEvent(userId, shareId, address.addressId)
                    }.onFailure { error ->
                        if (error is ShareNotAvailableError) {
                            onShareNotAvailable(userId, shareId)
                        } else {
                            PassLogger.w(TAG, "Error fetching pending events")
                            PassLogger.w(TAG, error)
                            throw error
                        }
                    }
                }
            }.awaitAll()
        }

        val eventSuccesses = eventResults
            .filter { eventResult -> eventResult.isSuccess }
            .mapNotNull { successEventResult -> successEventResult.getOrNull() }

        applyItemPendingEvents(eventSuccesses)
    }

    private suspend fun onShareNotAvailable(userId: UserId, shareId: ShareId) {
        PassLogger.i(TAG, "Deleting share not available")
        safeRunCatching {
            shareRepository.deleteVault(userId, shareId)
        }.onSuccess {
            PassLogger.i(TAG, "Deleted unavailable share id: $shareId")
        }.onFailure { error ->
            PassLogger.w(TAG, "Error deleting unavailable share id: $shareId")
            PassLogger.w(TAG, error)
            throw error
        }
    }

    private suspend fun fetchItemPendingEvent(
        userId: UserId,
        shareId: ShareId,
        addressId: AddressId
    ): ItemPendingEvent {
        val share = shareRepository.getById(userId, shareId)
        val pendingEventLists = mutableSetOf<PendingEventList>()
        var updateShareEvent: UpdateShareEvent? = null
        var lastEventId = eventRepository.getLatestEventId(userId, shareId)

        do {
            val eventList = eventRepository.getEvents(lastEventId, userId, shareId)
            lastEventId = eventList.latestEventId
            pendingEventLists.add(eventList.toDomain())
            eventList.shareResponse?.let { shareResponse ->
                updateShareEvent = shareResponse.toDomain(share.groupEmail)
            }
        } while (eventList.eventsPending)

        return ItemPendingEvent(
            userId = userId,
            shareId = shareId,
            addressId = addressId,
            lastEventId = lastEventId,
            updateShareEvent = updateShareEvent,
            pendingEventLists = pendingEventLists
        )
    }

    private suspend fun applyItemPendingEvents(events: List<ItemPendingEvent>) {
        events.forEach { event ->
            if (event.hasPendingChanges) {
                database.inTransaction(TRANSACTION_NAME_APPLY_PENDING_EVENTS) {
                    event.updateShareEvent?.let { updateShareEvent ->
                        shareRepository.applyPendingShareEvent(event.userId, updateShareEvent)
                        shareRepository.applyPendingShareEventKeys(event.userId, updateShareEvent)
                    }

                    itemRepository.applyPendingEvent(event)
                    itemRepository.purgePendingEvent(event)

                    eventRepository.storeLatestEventId(
                        userId = event.userId,
                        addressId = event.addressId,
                        shareId = event.shareId,
                        eventId = event.lastEventId
                    )
                }
            } else {
                eventRepository.storeLatestEventId(
                    userId = event.userId,
                    addressId = event.addressId,
                    shareId = event.shareId,
                    eventId = event.lastEventId
                )
            }
        }
    }

    private fun EventList.toDomain(): PendingEventList = PendingEventList(
        updatedItems = updatedItems.map { it.toPendingEvent() },
        deletedItemIds = deletedItemIds
    )

    private companion object {

        private const val TAG = "ApplyPendingEventsImpl"

    }

}

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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.extension.primary
import me.proton.core.user.domain.repository.UserAddressRepository
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.ItemPendingEvent
import proton.android.pass.data.api.PendingEventList
import proton.android.pass.data.api.errors.ShareNotAvailableError
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.repositories.ItemSyncStatus
import proton.android.pass.data.api.repositories.ItemSyncStatusRepository
import proton.android.pass.data.api.repositories.RefreshSharesResult
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.repositories.SyncMode
import proton.android.pass.data.api.repositories.UpdateShareEvent
import proton.android.pass.data.api.usecases.ApplyPendingEvents
import proton.android.pass.data.api.usecases.CreateVault
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.extensions.toDomain
import proton.android.pass.data.impl.extensions.toPendingEvent
import proton.android.pass.data.impl.repositories.EventRepository
import proton.android.pass.data.impl.responses.EventList
import proton.android.pass.data.impl.work.FetchItemsWorker
import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareIcon
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.entity.NewVault
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

private const val TRANSACTION_NAME_APPLY_PENDING_EVENTS = "ApplyPendingEvents"

class ApplyPendingEventsImpl @Inject constructor(
    private val database: PassDatabase,
    private val eventRepository: EventRepository,
    private val addressRepository: UserAddressRepository,
    private val itemRepository: ItemRepository,
    private val accountManager: AccountManager,
    private val shareRepository: ShareRepository,
    private val createVault: CreateVault,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val workManager: WorkManager,
    private val itemSyncStatusRepository: ItemSyncStatusRepository
) : ApplyPendingEvents {

    override suspend fun invoke(userId: UserId?) {
        PassLogger.i(TAG, "Applying pending events started")

        val currentUserId = userId ?: accountManager.getPrimaryUserId()
            .filterNotNull()
            .first()
        PassLogger.i(TAG, "Retrieved current user id")

        shareRepository.refreshShares(currentUserId).let { refreshSharesResult ->
            PassLogger.i(TAG, "Shares refreshed")
            if (refreshSharesResult.allShareIds.isEmpty()) {
                handleSharesWhenEmpty(currentUserId)
            } else {
                val address = requireNotNull(
                    addressRepository.getAddresses(currentUserId).primary()
                )
                PassLogger.i(TAG, "Retrieved user address")
                handleExistingShares(currentUserId, address.addressId, refreshSharesResult)
            }
        }
    }

    private suspend fun handleSharesWhenEmpty(userId: UserId) {
        PassLogger.i(TAG, "Received an empty list of shares, creating default vault")
        itemSyncStatusRepository.setMode(SyncMode.Background)
        createDefaultVault(userId)
        itemSyncStatusRepository.emit(ItemSyncStatus.CompletedSyncing(false))
    }

    private suspend fun handleExistingShares(
        userId: UserId,
        addressId: AddressId,
        refreshSharesResult: RefreshSharesResult,
    ) {
        PassLogger.i(TAG, "Received a list of shares, applying pending events")
        enqueueRefreshItems(refreshSharesResult.newShareIds)

        refreshSharesResult.allShareIds
            .subtract(refreshSharesResult.newShareIds)
            .let { existingShareIds ->
                coroutineScope {
                    existingShareIds.map { shareId ->
                        async {
                            runCatching { fetchItemPendingEvent(userId, shareId, addressId) }
                                .onFailure { error ->
                                    if (error is ShareNotAvailableError) {
                                        onShareNotAvailable(userId, shareId)
                                    }
                                }
                        }
                    }.let { deferredEventResults ->
                        deferredEventResults.awaitAll()
                            .filter { eventResult -> eventResult.isSuccess }
                            .mapNotNull { successEventResult -> successEventResult.getOrNull() }
                            .let { events -> applyItemPendingEvents(events) }
                    }
                }
            }
    }

    private suspend fun onShareNotAvailable(
        userId: UserId,
        shareId: ShareId
    ) {
        PassLogger.i(TAG, "Deleting share not available")
        runCatching {
            shareRepository.deleteVault(userId, shareId)
        }
            .onSuccess {
                PassLogger.i(TAG, "Deleted unavailable share id: $shareId")
            }
            .onFailure { t ->
                PassLogger.w(TAG, "Error deleting unavailable share id: $shareId")
                PassLogger.w(TAG, t)
            }
    }

    private suspend fun createDefaultVault(userId: UserId) {
        encryptionContextProvider.withEncryptionContext {
            NewVault(
                name = encrypt("Personal"),
                description = encrypt("Personal vault"),
                icon = ShareIcon.Icon1,
                color = ShareColor.Color1
            )
        }.let { vault ->
            runCatching { createVault(userId, vault) }
                .onFailure { error ->
                    PassLogger.w(TAG, "Error creating default vault")
                    PassLogger.w(TAG, error)
                }
                .onSuccess { PassLogger.i(TAG, "Default vault created") }
        }
    }

    private suspend fun fetchItemPendingEvent(
        userId: UserId,
        shareId: ShareId,
        addressId: AddressId,
    ): ItemPendingEvent {
        val pendingEventLists = mutableSetOf<PendingEventList>()
        var updateShareEvent: UpdateShareEvent? = null
        var lastEventId = eventRepository.getLatestEventId(userId, shareId)

        do {
            val eventList = eventRepository.getEvents(lastEventId, userId, shareId)
            lastEventId = eventList.latestEventId
            pendingEventLists.add(eventList.toDomain())
            eventList.shareResponse?.let { shareResponse ->
                updateShareEvent = shareResponse.toDomain()
            }
        } while (eventList.eventsPending)

        return ItemPendingEvent(
            userId = userId,
            shareId = shareId,
            addressId = addressId,
            lastEventId = lastEventId,
            updateShareEvent = updateShareEvent,
            pendingEventLists = pendingEventLists,
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
                        eventId = event.lastEventId,
                    )
                }
            } else {
                eventRepository.storeLatestEventId(
                    userId = event.userId,
                    addressId = event.addressId,
                    shareId = event.shareId,
                    eventId = event.lastEventId,
                )
            }
        }
    }

    private fun enqueueRefreshItems(shares: Set<ShareId>) {
        if (shares.isEmpty()) return

        FetchItemsWorker.getRequestFor(shares.toList())
            .let { fetchItemsWorkRequest ->
                PassLogger.i(TAG, "Enqueuing FetchItemsWorker")
                workManager.enqueue(fetchItemsWorkRequest)
            }
    }

    private fun EventList.toDomain(): PendingEventList = PendingEventList(
        updatedItems = updatedItems.map { it.toPendingEvent() },
        deletedItemIds = deletedItemIds
    )

    companion object {
        private const val TAG = "ApplyPendingEventsImpl"
    }
}

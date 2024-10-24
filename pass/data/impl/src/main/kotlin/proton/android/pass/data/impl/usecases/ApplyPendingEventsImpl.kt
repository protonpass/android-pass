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

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
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
import proton.android.pass.data.impl.R
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
    @ApplicationContext private val context: Context,
    private val database: PassDatabase,
    private val eventRepository: EventRepository,
    private val addressRepository: UserAddressRepository,
    private val itemRepository: ItemRepository,
    private val shareRepository: ShareRepository,
    private val createVault: CreateVault,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val workManager: WorkManager,
    private val itemSyncStatusRepository: ItemSyncStatusRepository
) : ApplyPendingEvents {

    override suspend fun invoke(userId: UserId) {
        PassLogger.i(TAG, "Applying pending events started")
        if (itemSyncStatusRepository.observeSyncState().first().isSyncing) {
            PassLogger.i(TAG, "Sync in progress, skipping")
            return
        }

        runCatching { shareRepository.refreshShares(userId) }
            .onFailure { error ->
                PassLogger.w(TAG, "Error refreshing shares")
                PassLogger.w(TAG, error)
                throw error
            }
            .onSuccess { refreshSharesResult ->
                PassLogger.i(TAG, "Shares for user: $userId refreshed")
                if (refreshSharesResult.allShareIds.isEmpty()) {
                    handleSharesWhenEmpty(userId)
                } else {
                    handleExistingShares(userId, refreshSharesResult)
                }
            }
    }

    private suspend fun handleSharesWhenEmpty(userId: UserId) {
        PassLogger.i(TAG, "Received an empty list of shares, creating default vault")

        runCatching { createDefaultVault(userId) }
            .onFailure { error ->
                PassLogger.w(TAG, "Error creating default vault")
                PassLogger.w(TAG, error)
                throw error
            }
            .onSuccess {
                PassLogger.i(TAG, "Default vault created")
                itemSyncStatusRepository.setMode(SyncMode.Background)
                itemSyncStatusRepository.emit(ItemSyncStatus.SyncSuccess)
            }
    }

    private suspend fun handleExistingShares(userId: UserId, refreshSharesResult: RefreshSharesResult) {
        PassLogger.i(TAG, "Received a list of shares, applying pending events")
        enqueueRefreshItems(
            userId = userId,
            shares = refreshSharesResult.newShareIds,
            wasFirstSync = refreshSharesResult.wasFirstSync
        )

        val existingShareIds = refreshSharesResult.allShareIds
            .subtract(refreshSharesResult.newShareIds)

        val address = requireNotNull(
            addressRepository.getAddresses(userId).primary()
        )

        val eventResults = coroutineScope {
            existingShareIds.map { shareId ->
                async {
                    runCatching {
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
        runCatching {
            shareRepository.deleteVault(userId, shareId)
        }.onSuccess {
            PassLogger.i(TAG, "Deleted unavailable share id: $shareId")
        }.onFailure { error ->
            PassLogger.w(TAG, "Error deleting unavailable share id: $shareId")
            PassLogger.w(TAG, error)
            throw error
        }
    }

    private suspend fun createDefaultVault(userId: UserId) {
        val vault = encryptionContextProvider.withEncryptionContextSuspendable {
            NewVault(
                name = encrypt(context.getString(R.string.vault_name)),
                description = encrypt(context.getString(R.string.vault_description)),
                icon = ShareIcon.Icon1,
                color = ShareColor.Color1
            )
        }
        createVault(userId, vault)
    }

    private suspend fun fetchItemPendingEvent(
        userId: UserId,
        shareId: ShareId,
        addressId: AddressId
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

    private fun enqueueRefreshItems(
        userId: UserId,
        shares: Set<ShareId>,
        wasFirstSync: Boolean
    ) {
        if (shares.isEmpty()) return

        val source = if (wasFirstSync) {
            FetchItemsWorker.FetchSource.FirstSync
        } else {
            FetchItemsWorker.FetchSource.NewShare
        }
        val request = FetchItemsWorker.getRequestFor(
            source = source,
            userId = userId,
            shareIds = shares.toList()
        )

        PassLogger.i(TAG, "Enqueuing FetchItemsWorker")
        workManager.enqueueUniqueWork(
            FetchItemsWorker.getOneTimeUniqueWorkName(userId),
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    private fun EventList.toDomain(): PendingEventList = PendingEventList(
        updatedItems = updatedItems.map { it.toPendingEvent() },
        deletedItemIds = deletedItemIds
    )

    private companion object {

        private const val TAG = "ApplyPendingEventsImpl"

    }

}

package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.extension.primary
import me.proton.core.user.domain.repository.UserAddressRepository
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.onError
import proton.android.pass.common.api.onSuccess
import proton.android.pass.common.api.runCatching
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.PendingEventList
import proton.android.pass.data.api.errors.ShareNotAvailableError
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.ApplyPendingEvents
import proton.android.pass.data.api.usecases.CreateVault
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.impl.extensions.toPendingEvent
import proton.android.pass.data.impl.repositories.EventRepository
import proton.android.pass.data.impl.responses.EventList
import proton.android.pass.log.api.PassLogger
import proton.pass.domain.ShareColor
import proton.pass.domain.ShareIcon
import proton.pass.domain.ShareId
import proton.pass.domain.entity.NewVault
import javax.inject.Inject

class ApplyPendingEventsImpl @Inject constructor(
    private val eventRepository: EventRepository,
    private val addressRepository: UserAddressRepository,
    private val itemRepository: ItemRepository,
    private val observeCurrentUser: ObserveCurrentUser,
    private val shareRepository: ShareRepository,
    private val createVault: CreateVault,
    private val encryptionContextProvider: EncryptionContextProvider
) : ApplyPendingEvents {

    override suspend fun invoke(): LoadingResult<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                val user = observeCurrentUser().first()
                val address = requireNotNull(addressRepository.getAddresses(user.userId).primary())
                val refreshSharesResult = shareRepository.refreshShares(user.userId)
                if (refreshSharesResult.allShareIds.isEmpty()) {
                    createDefaultVault(user.userId)
                } else {
                    refreshSharesResult.newShareIds
                        .map {
                            PassLogger.d(TAG, "Refreshing items on new share")
                            itemRepository.refreshItems(user.userId, it)
                            PassLogger.d(TAG, "Items refreshed on new share")
                        }
                    refreshSharesResult.allShareIds.subtract(refreshSharesResult.newShareIds)
                        .map { share ->
                            async {
                                try {
                                    applyPendingEvents(address.addressId, user.userId, share)
                                } catch (e: ShareNotAvailableError) {
                                    PassLogger.d(TAG, e, "Deleting share not available")
                                    shareRepository.deleteVault(user.userId, share)
                                }
                            }
                        }
                        .awaitAll()
                }
            }
        }

    private suspend fun createDefaultVault(userId: UserId) {
        PassLogger.d(TAG, "Creating default vault")

        val vault = encryptionContextProvider.withEncryptionContext {
            NewVault(
                name = encrypt("Personal"),
                description = encrypt("Personal vault"),
                icon = ShareIcon.Icon1,
                color = ShareColor.Color1
            )
        }
        createVault(userId, vault)
            .onSuccess { PassLogger.d(TAG, "Created default vault") }
            .onError { PassLogger.d(TAG, it, "Error creating default vault") }
    }

    private suspend fun applyPendingEvents(
        addressId: AddressId,
        userId: UserId,
        shareId: ShareId
    ) {
        while (true) {
            val events = eventRepository.getEvents(userId, addressId, shareId)

            PassLogger.d(TAG, "Applying events with share id :$shareId")
            itemRepository.applyEvents(
                userId,
                addressId,
                shareId,
                events.toDomain()
            )
            PassLogger.d(
                TAG,
                "Applied events with share id :$shareId. Storing latest event ID"
            )
            eventRepository.storeLatestEventId(
                userId,
                addressId,
                shareId,
                events.latestEventId
            )

            if (!events.eventsPending) break
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

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
import proton.android.pass.common.api.Result
import proton.android.pass.common.api.map
import proton.android.pass.common.api.runCatching
import proton.android.pass.data.api.PendingEventList
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.usecases.ApplyPendingEvents
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.android.pass.data.impl.extensions.toPendingEvent
import proton.android.pass.data.impl.repositories.EventRepository
import proton.android.pass.data.impl.responses.EventList
import proton.android.pass.log.api.PassLogger
import proton.pass.domain.ShareId
import javax.inject.Inject

class ApplyPendingEventsImpl @Inject constructor(
    private val eventRepository: EventRepository,
    private val addressRepository: UserAddressRepository,
    private val itemRepository: ItemRepository,
    private val observeCurrentUser: ObserveCurrentUser,
    private val observeVaults: ObserveVaults
) : ApplyPendingEvents {

    override suspend fun invoke(): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                val user = observeCurrentUser().first()
                val address = requireNotNull(addressRepository.getAddresses(user.userId).primary())
                observeVaults()
                    .first()
                    .map { vaults ->
                        vaults.map { vault ->
                            async {
                                applyPendingEvents(address.addressId, user.userId, vault.shareId)
                            }
                        }.awaitAll()
                    }
            }
        }

    private suspend fun applyPendingEvents(addressId: AddressId, userId: UserId, shareId: ShareId) {
        while (true) {
            val events = eventRepository.getEvents(userId, addressId, shareId)
            if (events.fullRefresh) {
                PassLogger.i(TAG, "Performing full refresh with share id :$shareId")
                itemRepository.refreshItems(userId, shareId)
                PassLogger.i(TAG, "Finished full refresh with share id :$shareId")
            } else {
                PassLogger.i(TAG, "Applying events with share id :$shareId")
                itemRepository.applyEvents(
                    userId,
                    addressId,
                    shareId,
                    events.toDomain()
                )
                PassLogger.i(
                    TAG,
                    "Applied events with share id :$shareId. Storing latest event ID"
                )
                eventRepository.storeLatestEventId(
                    userId,
                    addressId,
                    shareId,
                    events.latestEventId
                )
            }
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

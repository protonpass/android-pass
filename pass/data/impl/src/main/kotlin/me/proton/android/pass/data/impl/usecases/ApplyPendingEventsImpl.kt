package me.proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.proton.android.pass.data.api.PendingEventList
import me.proton.android.pass.data.api.repositories.ItemRepository
import me.proton.android.pass.data.api.usecases.ApplyPendingEvents
import me.proton.android.pass.data.impl.extensions.toDomain
import me.proton.android.pass.data.impl.repositories.EventRepository
import me.proton.android.pass.data.impl.responses.EventList
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.extension.primary
import me.proton.core.user.domain.repository.UserAddressRepository
import me.proton.pass.domain.ShareId
import javax.inject.Inject

class ApplyPendingEventsImpl @Inject constructor(
    private val eventRepository: EventRepository,
    private val addressRepository: UserAddressRepository,
    private val itemRepository: ItemRepository
) : ApplyPendingEvents {
    override fun invoke(userId: UserId, shareId: ShareId): Flow<Unit> = flow {
        val address = requireNotNull(addressRepository.getAddresses(userId).primary())
        while (true) {
            val events = eventRepository.getEvents(userId, address.addressId, shareId)
            if (events.fullRefresh) {
                itemRepository.refreshItems(userId, shareId)
            } else {
                itemRepository.applyEvents(userId, address.addressId, shareId, events.toDomain())
                eventRepository.storeLatestEventId(userId, address.addressId, shareId, events.latestEventId)
            }

            if (!events.eventsPending) break
        }

        emit(Unit)
    }

    private fun EventList.toDomain(): PendingEventList = PendingEventList(
        updatedItems = updatedItems.map { it.toDomain() },
        deletedItemIds = deletedItemIds
    )
}

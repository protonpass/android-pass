package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import proton.android.pass.data.api.ItemCountSummary
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.usecases.ObserveAllShares
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.ObserveItemCount
import proton.pass.domain.ItemState
import javax.inject.Inject

class ObserveItemCountImpl @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUser,
    private val observeAllShares: ObserveAllShares,
    private val itemRepository: ItemRepository
) : ObserveItemCount {

    override fun invoke(itemState: ItemState?): Flow<ItemCountSummary> = observeAllShares()
        .flatMapLatest { items ->
            observeCurrentUser()
                .flatMapLatest { user ->
                    itemRepository.observeItemCountSummary(
                        userId = user.userId,
                        shareIds = items.map { it.id },
                        itemState = itemState
                    )
                }
        }
}

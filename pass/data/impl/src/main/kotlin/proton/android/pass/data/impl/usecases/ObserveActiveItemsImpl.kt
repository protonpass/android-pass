package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import proton.android.pass.common.api.Result
import proton.android.pass.common.api.map
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveActiveItems
import proton.android.pass.data.api.usecases.ObserveAllShares
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.pass.domain.Item
import proton.pass.domain.ItemState
import proton.pass.domain.ShareSelection
import javax.inject.Inject

class ObserveActiveItemsImpl @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUser,
    private val observeAllShares: ObserveAllShares,
    private val itemRepository: ItemRepository
) : ObserveActiveItems {

    override operator fun invoke(
        filter: ItemTypeFilter,
        shareSelection: ShareSelection
    ): Flow<Result<List<Item>>> = observeCurrentUser()
        .filterNotNull()
        .combine(observeAllShares()) { user, shares ->
            user to shares.map { share -> share.map { shareId -> shareId.id } }
        }
        .distinctUntilChanged()
        .flatMapLatest { pair ->
            itemRepository.observeItems(
                userId = pair.first.userId,
                shareSelection = shareSelection,
                itemState = ItemState.Active,
                itemTypeFilter = filter
            )
        }
        .distinctUntilChanged()
}

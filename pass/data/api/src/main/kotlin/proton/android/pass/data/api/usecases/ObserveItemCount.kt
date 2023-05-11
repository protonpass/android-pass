package proton.android.pass.data.api.usecases

import kotlinx.coroutines.flow.Flow
import proton.android.pass.data.api.ItemCountSummary
import proton.pass.domain.ItemState

interface ObserveItemCount {
    operator fun invoke(itemState: ItemState? = ItemState.Active): Flow<ItemCountSummary>
}

package proton.android.pass.data.fakes.usecases

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveActiveItems
import proton.pass.domain.Item
import proton.pass.domain.ShareSelection
import javax.inject.Inject

class TestObserveActiveItems @Inject constructor() : ObserveActiveItems {

    private val activeItemsFlow: MutableSharedFlow<LoadingResult<List<Item>>> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override fun invoke(
        filter: ItemTypeFilter,
        shareSelection: ShareSelection
    ): Flow<LoadingResult<List<Item>>> = activeItemsFlow

    fun sendItemList(result: LoadingResult<List<Item>>) = activeItemsFlow.tryEmit(result)

}

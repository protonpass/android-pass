package proton.android.pass.data.fakes.usecases

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveActiveItems
import proton.android.pass.common.api.Result
import proton.pass.domain.Item
import javax.inject.Inject

class TestObserveActiveItems @Inject constructor() : ObserveActiveItems {

    private val activeItemsFlow: MutableSharedFlow<Result<List<Item>>> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override fun invoke(filter: ItemTypeFilter): Flow<Result<List<Item>>> = activeItemsFlow

    fun sendItemList(result: Result<List<Item>>) = activeItemsFlow.tryEmit(result)

}

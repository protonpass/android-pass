package proton.android.pass.data.fakes.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onSubscription
import proton.android.pass.common.api.FlowUtils.testFlow
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.toOption
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveActiveItems
import proton.pass.domain.Item
import proton.pass.domain.ShareSelection
import javax.inject.Inject

class TestObserveActiveItems @Inject constructor() : ObserveActiveItems {

    private var exceptionOption: Option<Exception> = None
    private val itemsFlow = testFlow<List<Item>>()

    private val memory = mutableListOf<Payload>()

    fun getMemory(): List<Payload> = memory

    override fun invoke(
        filter: ItemTypeFilter,
        shareSelection: ShareSelection
    ): Flow<List<Item>> {
        memory.add(Payload(filter, shareSelection))
        return itemsFlow.onSubscription {
            when (val exception = exceptionOption) {
                None -> {}
                is Some -> throw exception.value
            }
        }
    }

    fun sendItemList(items: List<Item>) = itemsFlow.tryEmit(items)

    fun sendException(exception: Exception) {
        exceptionOption = exception.toOption()
    }

    data class Payload(
        val filter: ItemTypeFilter,
        val shareSelection: ShareSelection
    )
}

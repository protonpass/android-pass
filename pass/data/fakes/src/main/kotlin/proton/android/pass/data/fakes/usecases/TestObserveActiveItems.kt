package proton.android.pass.data.fakes.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveActiveItems
import proton.pass.domain.Item
import proton.pass.domain.ShareSelection
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestObserveActiveItems @Inject constructor() : ObserveActiveItems {

    private val itemsFlow = MutableStateFlow<Result<List<Item>>>(Result.success(emptyList()))

    private val memory = mutableListOf<Payload>()

    fun getMemory(): List<Payload> = memory

    override fun invoke(
        filter: ItemTypeFilter,
        shareSelection: ShareSelection
    ): Flow<List<Item>> {
        memory.add(Payload(filter, shareSelection))
        return itemsFlow.map { it.getOrThrow() }
    }

    fun sendItemList(items: List<Item>) = itemsFlow.tryEmit(Result.success(items))

    fun sendException(exception: Exception) {
        itemsFlow.tryEmit(Result.failure(exception))
    }

    data class Payload(
        val filter: ItemTypeFilter,
        val shareSelection: ShareSelection
    )
}

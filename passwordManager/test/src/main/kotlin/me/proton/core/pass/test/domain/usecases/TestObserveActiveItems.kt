package me.proton.core.pass.test.domain.usecases

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import me.proton.core.pass.domain.Item
import me.proton.core.pass.domain.usecases.ObserveActiveItems

class TestObserveActiveItems : ObserveActiveItems {

    private val activeItemsFlow: MutableSharedFlow<List<Item>> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override fun invoke(): Flow<List<Item>> = activeItemsFlow

    fun sendItemList(items: List<Item>) = activeItemsFlow.tryEmit(items)

}

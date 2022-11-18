package me.proton.pass.test.domain.usecases

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import me.proton.android.pass.data.api.usecases.ObserveActiveItems
import me.proton.pass.common.api.Result
import me.proton.pass.domain.Item

class TestObserveActiveItems : ObserveActiveItems {

    private val activeItemsFlow: MutableSharedFlow<Result<List<Item>>> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override fun invoke(): Flow<Result<List<Item>>> = activeItemsFlow

    fun sendItemList(result: Result<List<Item>>) = activeItemsFlow.tryEmit(result)

}

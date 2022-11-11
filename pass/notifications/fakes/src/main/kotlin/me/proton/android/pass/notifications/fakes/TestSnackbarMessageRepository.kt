package me.proton.android.pass.notifications.fakes

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import me.proton.android.pass.notifications.api.SnackbarMessage
import me.proton.android.pass.notifications.api.SnackbarMessageRepository
import me.proton.pass.common.api.None
import me.proton.pass.common.api.Option
import me.proton.pass.common.api.some

class TestSnackbarMessageRepository : SnackbarMessageRepository {
    private val snackbarState = MutableSharedFlow<Option<SnackbarMessage>>(
        replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override val snackbarMessage: Flow<Option<SnackbarMessage>> = snackbarState

    override suspend fun emitSnackbarMessage(snackbarMessage: SnackbarMessage) {
        snackbarState.tryEmit(snackbarMessage.some())
    }

    override suspend fun snackbarMessageDelivered() {
        snackbarState.tryEmit(None)
    }
}

package proton.android.pass.notifications.fakes

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.some
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.notifications.api.SnackbarMessageRepository
import javax.inject.Inject

class TestSnackbarMessageRepository @Inject constructor() : SnackbarMessageRepository {
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

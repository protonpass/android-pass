package proton.android.pass.notifications.fakes

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.some
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.notifications.api.SnackbarMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestSnackbarDispatcher @Inject constructor() : SnackbarDispatcher {
    private val snackbarState = MutableSharedFlow<Option<SnackbarMessage>>(
        replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override val snackbarMessage: Flow<Option<SnackbarMessage>> = snackbarState

    override suspend fun invoke(snackbarMessage: SnackbarMessage) {
        snackbarState.tryEmit(snackbarMessage.some())
    }

    override suspend fun snackbarMessageDelivered() {
        snackbarState.tryEmit(None)
    }
}

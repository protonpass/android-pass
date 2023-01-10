package proton.android.pass.notifications.implementation

import android.os.Build
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.some
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.notifications.api.SnackbarMessageRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SnackbarMessageRepositoryImpl @Inject constructor() : SnackbarMessageRepository {
    private val mutex = Mutex()

    private val snackbarState = MutableStateFlow<Option<SnackbarMessage>>(None)

    override val snackbarMessage: Flow<Option<SnackbarMessage>> = snackbarState

    override suspend fun emitSnackbarMessage(snackbarMessage: SnackbarMessage) {
        if (!shouldDisplay(snackbarMessage)) return
        mutex.withLock {
            snackbarState.update { snackbarMessage.some() }
        }
    }

    override suspend fun snackbarMessageDelivered() {
        mutex.withLock {
            snackbarState.update { None }
        }
    }

    private fun shouldDisplay(snackbarMessage: SnackbarMessage): Boolean =
        if (snackbarMessage.isClipboard && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            false
        } else {
            true
        }
}

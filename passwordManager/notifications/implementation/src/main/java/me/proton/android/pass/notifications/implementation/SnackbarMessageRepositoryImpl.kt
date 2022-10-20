package me.proton.android.pass.notifications.implementation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.proton.android.pass.notifications.api.SnackbarMessage
import me.proton.android.pass.notifications.api.SnackbarMessageRepository
import me.proton.core.pass.common.api.None
import me.proton.core.pass.common.api.Option
import me.proton.core.pass.common.api.some
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SnackbarMessageRepositoryImpl @Inject constructor() : SnackbarMessageRepository {
    private val mutex = Mutex()

    private val snackbarState = MutableStateFlow<Option<SnackbarMessage>>(None)

    override val snackbarMessage: Flow<Option<SnackbarMessage>> = snackbarState

    override suspend fun emitSnackbarMessage(snackbarMessage: SnackbarMessage) {
        mutex.withLock {
            snackbarState.update { snackbarMessage.some() }
        }
    }

    override suspend fun snackbarMessageDelivered() {
        mutex.withLock {
            snackbarState.update { None }
        }
    }
}

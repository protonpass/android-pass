package me.proton.core.pass.test.notification

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.proton.android.pass.notifications.api.SnackbarMessage
import me.proton.android.pass.notifications.api.SnackbarMessageRepository
import me.proton.core.pass.common.api.None
import me.proton.core.pass.common.api.Option
import me.proton.core.pass.common.api.some

class TestSnackbarMessageRepository : SnackbarMessageRepository {
    private val snackbarState = MutableStateFlow<Option<SnackbarMessage>>(None)

    override val snackbarMessage: Flow<Option<SnackbarMessage>> = snackbarState

    override suspend fun emitSnackbarMessage(snackbarMessage: SnackbarMessage) {
        snackbarState.update { snackbarMessage.some() }
    }

    override suspend fun snackbarMessageDelivered() {
        snackbarState.update { None }
    }
}

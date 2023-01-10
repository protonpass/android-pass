package proton.android.pass.notifications.api

import kotlinx.coroutines.flow.Flow
import proton.android.pass.common.api.Option

interface SnackbarMessageRepository {
    val snackbarMessage: Flow<Option<SnackbarMessage>>
    suspend fun emitSnackbarMessage(snackbarMessage: SnackbarMessage)
    suspend fun snackbarMessageDelivered()
}

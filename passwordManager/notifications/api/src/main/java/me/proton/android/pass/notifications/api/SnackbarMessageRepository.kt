package me.proton.android.pass.notifications.api

import kotlinx.coroutines.flow.Flow
import me.proton.core.pass.common.api.Option

interface SnackbarMessageRepository {
    val snackbarMessage: Flow<Option<SnackbarMessage>>
    suspend fun emitSnackbarMessage(snackbarMessage: SnackbarMessage)
    suspend fun snackbarMessageDelivered()
}

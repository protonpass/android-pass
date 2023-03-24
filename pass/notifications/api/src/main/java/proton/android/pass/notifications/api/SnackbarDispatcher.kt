package proton.android.pass.notifications.api

import kotlinx.coroutines.flow.Flow
import proton.android.pass.common.api.Option

interface SnackbarDispatcher {
    val snackbarMessage: Flow<Option<SnackbarMessage>>
    suspend operator fun invoke(snackbarMessage: SnackbarMessage)
    suspend fun snackbarMessageDelivered()
}

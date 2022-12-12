package me.proton.pass.presentation.trash

import androidx.annotation.StringRes
import me.proton.android.pass.notifications.api.SnackbarMessage
import me.proton.android.pass.notifications.api.SnackbarType
import me.proton.pass.presentation.R

enum class TrashSnackbarMessage(
    @StringRes override val id: Int,
    override val type: SnackbarType
) : SnackbarMessage {
    ObserveItemsError(R.string.error_observing_items, SnackbarType.ERROR),
    ClearTrashError(R.string.error_clearing_trash, SnackbarType.ERROR),
    RestoreItemsError(R.string.error_restoring_items, SnackbarType.ERROR),
    DeleteItemError(R.string.error_deleting_item, SnackbarType.ERROR),
    RefreshError(R.string.error_refreshing, SnackbarType.ERROR)
}

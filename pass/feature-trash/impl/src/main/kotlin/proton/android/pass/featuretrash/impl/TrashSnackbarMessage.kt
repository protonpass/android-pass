package proton.android.pass.featuretrash.impl

import androidx.annotation.StringRes
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.notifications.api.SnackbarType

enum class TrashSnackbarMessage(
    @StringRes override val id: Int,
    override val type: SnackbarType,
    override val isClipboard: Boolean = false
) : SnackbarMessage {
    ObserveItemsError(R.string.trash_error_observing_items, SnackbarType.ERROR),
    ClearTrashError(R.string.trash_error_clearing_trash, SnackbarType.ERROR),
    RestoreItemsError(R.string.trash_error_restoring_items, SnackbarType.ERROR),
    DeleteItemError(R.string.trash_error_deleting_item, SnackbarType.ERROR),
    RefreshError(R.string.trash_error_refreshing, SnackbarType.ERROR)
}

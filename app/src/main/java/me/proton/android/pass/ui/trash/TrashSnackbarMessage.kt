package me.proton.android.pass.ui.trash

import androidx.annotation.StringRes
import me.proton.android.pass.R
import me.proton.android.pass.notifications.api.SnackbarMessage
import me.proton.android.pass.notifications.api.SnackbarType

enum class TrashSnackbarMessage(
    @StringRes override val id: Int,
    override val type: SnackbarType
) : SnackbarMessage {
    ObserveItemsError(R.string.error_observing_items, SnackbarType.ERROR),
    ClearTrashError(R.string.error_clearing_trash, SnackbarType.ERROR),
    DeleteItemError(R.string.error_deleting_item, SnackbarType.ERROR),
    RefreshError(R.string.error_refreshing, SnackbarType.ERROR)
}

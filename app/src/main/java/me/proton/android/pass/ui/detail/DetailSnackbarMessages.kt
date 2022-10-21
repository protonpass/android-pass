package me.proton.android.pass.ui.detail

import androidx.annotation.StringRes
import me.proton.android.pass.R
import me.proton.android.pass.notifications.api.SnackbarMessage
import me.proton.android.pass.notifications.api.SnackbarType

enum class DetailSnackbarMessages(
    @StringRes override val id: Int,
    override val type: SnackbarType
) : SnackbarMessage {
    InitError(R.string.detail_init_error, SnackbarType.ERROR),
    SendToTrashError(R.string.detail_send_to_trash_error, SnackbarType.ERROR),
    ItemNotLoadedError(R.string.detail_item_not_loaded_error, SnackbarType.ERROR)
}

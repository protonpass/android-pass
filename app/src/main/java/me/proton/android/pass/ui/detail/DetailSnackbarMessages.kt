package me.proton.android.pass.ui.detail

import androidx.annotation.StringRes
import me.proton.android.pass.R

enum class DetailSnackbarMessages(@StringRes val id: Int) {
    InitError(R.string.detail_init_error),
    SendToTrashError(R.string.detail_send_to_trash_error),
    ItemNotLoadedError(R.string.detail_item_not_loaded_error)
}

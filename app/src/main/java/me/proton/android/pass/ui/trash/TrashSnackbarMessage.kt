package me.proton.android.pass.ui.trash

import androidx.annotation.StringRes
import me.proton.android.pass.R

enum class TrashSnackbarMessage(@StringRes val id: Int) {
    ObserveItemsError(R.string.error_observing_items),
    ClearTrashError(R.string.error_clearing_trash),
    DeleteItemError(R.string.error_deleting_item),
    RefreshError(R.string.error_refreshing)
}

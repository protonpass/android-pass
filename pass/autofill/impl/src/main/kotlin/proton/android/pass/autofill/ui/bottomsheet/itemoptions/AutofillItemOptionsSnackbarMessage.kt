package proton.android.pass.autofill.ui.bottomsheet.itemoptions

import androidx.annotation.StringRes
import proton.android.pass.autofill.service.R
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.notifications.api.SnackbarType

enum class AutofillItemOptionsSnackbarMessage(
    @StringRes override val id: Int,
    override val type: SnackbarType,
    override val isClipboard: Boolean = false
) : SnackbarMessage {
    SentToTrashSuccess(R.string.snackbar_item_move_to_trash_success, SnackbarType.SUCCESS),
    SentToTrashError(R.string.snackbar_item_move_to_trash_error, SnackbarType.ERROR)
}

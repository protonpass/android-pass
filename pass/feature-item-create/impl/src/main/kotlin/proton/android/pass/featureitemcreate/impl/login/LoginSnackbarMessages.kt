package proton.android.pass.featureitemcreate.impl.login

import androidx.annotation.StringRes
import proton.android.pass.featureitemcreate.impl.R
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.notifications.api.SnackbarType

enum class LoginSnackbarMessages(
    @StringRes override val id: Int,
    override val type: SnackbarType,
    override val isClipboard: Boolean = false
) : SnackbarMessage {
    InitError(R.string.create_login_init_error, SnackbarType.ERROR),
    ItemCreationError(R.string.create_login_item_creation_error, SnackbarType.ERROR),
    ItemUpdateError(R.string.create_login_item_update_error, SnackbarType.ERROR),
    LoginCreated(R.string.login_created, SnackbarType.SUCCESS),
    LoginUpdated(R.string.changes_saved, SnackbarType.SUCCESS),
}

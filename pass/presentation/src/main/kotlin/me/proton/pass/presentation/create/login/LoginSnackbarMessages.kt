package me.proton.pass.presentation.create.login

import androidx.annotation.StringRes
import me.proton.android.pass.notifications.api.SnackbarMessage
import me.proton.android.pass.notifications.api.SnackbarType
import me.proton.pass.presentation.R

enum class LoginSnackbarMessages(
    @StringRes override val id: Int,
    override val type: SnackbarType
) : SnackbarMessage {
    EmptyShareIdError(R.string.create_login_empty_share_id, SnackbarType.ERROR),
    InitError(R.string.create_login_init_error, SnackbarType.ERROR),
    ItemCreationError(R.string.create_login_item_creation_error, SnackbarType.ERROR),
    ItemUpdateError(R.string.create_login_item_update_error, SnackbarType.ERROR),
    LoginCreated(R.string.login_created, SnackbarType.SUCCESS),
    LoginUpdated(R.string.changes_saved, SnackbarType.SUCCESS)
}

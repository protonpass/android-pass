package me.proton.core.pass.presentation.create.login

import androidx.annotation.StringRes
import me.proton.core.pass.presentation.R

enum class LoginSnackbarMessages(@StringRes val id: Int) {
    EmptyShareIdError(R.string.create_login_empty_share_id),
    InitError(R.string.create_login_init_error),
    ItemCreationError(R.string.create_login_item_creation_error),
    ItemUpdateError(R.string.create_login_item_update_error)
}

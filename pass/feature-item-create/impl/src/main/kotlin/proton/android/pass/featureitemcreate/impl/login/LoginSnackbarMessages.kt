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
    CannotCreateMoreAliases(R.string.create_alias_cannot_create_more_aliases_error, SnackbarType.ERROR),
    EmailNotValidated(R.string.create_alias_email_not_validated_error, SnackbarType.ERROR),
    AliasRateLimited(R.string.alias_rate_limited, SnackbarType.ERROR),
    InvalidTotpError(R.string.create_login_invalid_totp, SnackbarType.ERROR),
}

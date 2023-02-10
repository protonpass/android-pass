package proton.android.pass.featureitemdetail.impl

import androidx.annotation.StringRes
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.notifications.api.SnackbarType

enum class DetailSnackbarMessages(
    @StringRes override val id: Int,
    override val type: SnackbarType,
    override val isClipboard: Boolean = false
) : SnackbarMessage {
    InitError(R.string.detail_init_error, SnackbarType.ERROR),
    ItemNotLoadedError(R.string.detail_item_not_loaded_error, SnackbarType.ERROR),
    AliasCopiedToClipboard(R.string.alias_copied_to_clipboard, SnackbarType.NORM, true),
    NoteCopiedToClipboard(R.string.note_copied_to_clipboard, SnackbarType.NORM, true),
    UsernameCopiedToClipboard(R.string.username_copied_to_clipboard, SnackbarType.NORM, true),
    PasswordCopiedToClipboard(R.string.password_copied_to_clipboard, SnackbarType.NORM, true),
    WebsiteCopiedToClipbopard(R.string.website_copied_to_clipboard, SnackbarType.NORM, true),
    TotpCopiedToClipbopard(R.string.totp_copied_to_clipboard, SnackbarType.NORM, true)
}

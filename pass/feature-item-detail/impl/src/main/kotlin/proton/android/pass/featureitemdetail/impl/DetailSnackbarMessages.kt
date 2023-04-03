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
    AliasCopiedToClipboard(R.string.alias_copied_to_clipboard, SnackbarType.NORM, true),
    UsernameCopiedToClipboard(R.string.username_copied_to_clipboard, SnackbarType.NORM, true),
    PasswordCopiedToClipboard(R.string.password_copied_to_clipboard, SnackbarType.NORM, true),
    WebsiteCopiedToClipboard(R.string.website_copied_to_clipboard, SnackbarType.NORM, true),
    TotpCopiedToClipboard(R.string.totp_copied_to_clipboard, SnackbarType.NORM, true),
    ItemMovedToTrash(R.string.move_item_to_trash_message, SnackbarType.NORM),
    ItemNotMovedToTrash(R.string.error_move_item_to_trash_message, SnackbarType.ERROR),
    ItemPermanentlyDeleted(R.string.item_permanently_deleted_message, SnackbarType.NORM),
    ItemNotPermanentlyDeleted(R.string.item_permanently_deleted_message_error, SnackbarType.ERROR),
    ItemRestored(R.string.item_restored_message, SnackbarType.NORM),
    ItemNotRestored(R.string.item_restored_message_error, SnackbarType.ERROR)
}

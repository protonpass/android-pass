package proton.android.pass.featureitemcreate.impl.login.bottomsheet.password

import androidx.annotation.StringRes
import proton.android.pass.featureitemcreate.impl.R
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.notifications.api.SnackbarType

enum class GeneratePasswordSnackbarMessage(
    @StringRes override val id: Int,
    override val type: SnackbarType,
    override val isClipboard: Boolean = false
) : SnackbarMessage {
    CopiedToClipboard(R.string.password_copied_to_clipboard, SnackbarType.NORM)
}

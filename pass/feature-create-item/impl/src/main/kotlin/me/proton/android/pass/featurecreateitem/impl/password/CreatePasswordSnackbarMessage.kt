package me.proton.android.pass.featurecreateitem.impl.password

import androidx.annotation.StringRes
import me.proton.android.pass.featurecreateitem.impl.R
import me.proton.android.pass.notifications.api.SnackbarMessage
import me.proton.android.pass.notifications.api.SnackbarType

enum class CreatePasswordSnackbarMessage(
    @StringRes override val id: Int,
    override val type: SnackbarType,
    override val isClipboard: Boolean = false
) : SnackbarMessage {
    CopiedToClipboard(R.string.password_copied_to_clipboard, SnackbarType.NORM)
}

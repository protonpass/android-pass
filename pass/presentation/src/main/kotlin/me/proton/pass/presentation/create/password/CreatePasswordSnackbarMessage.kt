package me.proton.pass.presentation.create.password

import androidx.annotation.StringRes
import me.proton.android.pass.notifications.api.SnackbarMessage
import me.proton.android.pass.notifications.api.SnackbarType
import me.proton.pass.presentation.R

enum class CreatePasswordSnackbarMessage(
    @StringRes override val id: Int,
    override val type: SnackbarType
) : SnackbarMessage {
    CopiedToClipboard(R.string.password_copied_to_clipboard, SnackbarType.NORM)
}

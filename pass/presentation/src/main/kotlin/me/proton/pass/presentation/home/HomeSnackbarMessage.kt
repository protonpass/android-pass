package me.proton.pass.presentation.home

import androidx.annotation.StringRes
import me.proton.android.pass.notifications.api.SnackbarMessage
import me.proton.android.pass.notifications.api.SnackbarType
import me.proton.pass.presentation.R

enum class HomeSnackbarMessage(
    @StringRes override val id: Int,
    override val type: SnackbarType
) : SnackbarMessage {
    ObserveItemsError(R.string.error_observing_items, SnackbarType.ERROR),
    ObserveShareError(R.string.error_observing_share, SnackbarType.ERROR),
    RefreshError(R.string.error_refreshing, SnackbarType.ERROR),
    AliasCopied(R.string.home_snackbar_alias_copied, SnackbarType.NORM),
    NoteCopied(R.string.home_snackbar_note_copied, SnackbarType.NORM),
    PasswordCopied(R.string.home_snackbar_password_copied, SnackbarType.NORM),
    UsernameCopied(R.string.home_snackbar_username_copied, SnackbarType.NORM),
}


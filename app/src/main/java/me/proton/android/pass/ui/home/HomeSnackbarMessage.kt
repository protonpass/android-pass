package me.proton.android.pass.ui.home

import androidx.annotation.StringRes
import me.proton.android.pass.R
import me.proton.android.pass.notifications.api.SnackbarMessage
import me.proton.android.pass.notifications.api.SnackbarType

enum class HomeSnackbarMessage(
    @StringRes override val id: Int,
    override val type: SnackbarType
) : SnackbarMessage {
    ObserveItemsError(R.string.error_observing_items, SnackbarType.ERROR),
    ObserveShareError(R.string.error_observing_share, SnackbarType.ERROR),
    RefreshError(R.string.error_refreshing, SnackbarType.ERROR)
}


package me.proton.core.pass.autofill.ui.autofill.select

import androidx.annotation.StringRes
import me.proton.android.pass.notifications.api.SnackbarMessage
import me.proton.android.pass.notifications.api.SnackbarType
import me.proton.core.pass.autofill.service.R

enum class SelectItemSnackbarMessage(
    @StringRes override val id: Int,
    override val type: SnackbarType
) : SnackbarMessage {
    LoadItemsError(R.string.error_loading_items, SnackbarType.ERROR),
}


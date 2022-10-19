package me.proton.core.pass.autofill.ui.autofill.select

import androidx.annotation.StringRes
import me.proton.core.pass.autofill.service.R

enum class SelectItemSnackbarMessage(@StringRes val id: Int) {
    LoadItemsError(R.string.error_loading_items),
}


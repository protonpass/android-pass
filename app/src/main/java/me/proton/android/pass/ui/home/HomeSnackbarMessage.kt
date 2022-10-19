package me.proton.android.pass.ui.home

import androidx.annotation.StringRes
import me.proton.android.pass.R

enum class HomeSnackbarMessage(@StringRes val id: Int) {
    ObserveItemsError(R.string.error_observing_items),
    ObserveShareError(R.string.error_observing_share),
    RefreshError(R.string.error_refreshing)
}


package me.proton.android.pass.ui.home.bottomsheet

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import me.proton.android.pass.R

@Stable
enum class SortingType(@StringRes val title: Int) {
    ByName(R.string.sort_by_name),
    ByItemType(R.string.sort_by_type)
}

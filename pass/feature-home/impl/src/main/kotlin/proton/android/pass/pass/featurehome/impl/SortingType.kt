package proton.android.pass.pass.featurehome.impl

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable

@Stable
enum class SortingType(@StringRes val titleId: Int) {
    ByName(R.string.sort_by_name),
    ByItemType(R.string.sort_by_type),
    ByModificationDate(R.string.sort_by_modified)
}

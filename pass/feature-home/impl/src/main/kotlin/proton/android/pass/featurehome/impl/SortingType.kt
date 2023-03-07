package proton.android.pass.featurehome.impl

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable

@Stable
enum class SortingType(@StringRes val titleId: Int) {
    MostRecent(R.string.sort_by_modification_date),
    TitleAsc(R.string.sort_by_title_asc),
    TitleDesc(R.string.sort_by_title_desc),
    CreationAsc(R.string.sort_by_creation_asc),
    CreationDesc(R.string.sort_by_creation_desc),
}

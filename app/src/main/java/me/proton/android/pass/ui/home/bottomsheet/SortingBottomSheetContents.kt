package me.proton.android.pass.ui.home.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import me.proton.android.pass.R
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetTitle

@ExperimentalMaterialApi
@Composable
fun SortingBottomSheetContents(
    modifier: Modifier = Modifier,
    sortingType: SortingType = SortingType.ByName,
    onSortingTypeSelected: (SortingType) -> Unit
) {
    val sortingTypes = SortingType.values().toList().map { it to stringResource(id = it.title) }
    Column(modifier) {
        BottomSheetTitle(title = R.string.sorting_bottomsheet_title)
        SortingTypeBottomSheetItemList(
            items = sortingTypes,
            displayer = { it.second },
            isChecked = { it.first == sortingType },
            onSelect = { onSortingTypeSelected(it.first) }
        )
    }
}

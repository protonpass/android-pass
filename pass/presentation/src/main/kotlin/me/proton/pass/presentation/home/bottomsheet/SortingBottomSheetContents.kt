package me.proton.pass.presentation.home.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.commonui.api.ThemePreviewProvider
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetItem
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetItemIcon
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetItemList
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetItemTitle
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetTitle
import me.proton.pass.presentation.home.SortingType

@ExperimentalMaterialApi
@Composable
fun SortingBottomSheetContents(
    modifier: Modifier = Modifier,
    sortingType: SortingType = SortingType.ByName,
    onSortingTypeSelected: (SortingType) -> Unit
) {
    Column(modifier) {
        BottomSheetTitle(title = R.string.sorting_bottomsheet_title)
        BottomSheetItemList(
            items = sortingItemList(sortingType, onSortingTypeSelected)
        )
    }
}

private fun sortingItemList(
    selectedSortingType: SortingType,
    onSortingTypeSelected: (SortingType) -> Unit
): List<BottomSheetItem> =
    SortingType.values()
        .map {
            object : BottomSheetItem {
                override val title: @Composable () -> Unit
                    get() = { BottomSheetItemTitle(text = stringResource(id = it.titleId)) }
                override val subtitle: (() -> Unit)?
                    get() = null
                override val icon: @Composable (() -> Unit)?
                    get() = if (it == selectedSortingType) {
                        { BottomSheetItemIcon(iconId = me.proton.core.presentation.R.drawable.ic_proton_checkmark) }
                    } else null
                override val onClick: () -> Unit
                    get() = { onSortingTypeSelected(it) }
            }
        }

@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun SortingBottomSheetContentsPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    ProtonTheme(isDark = isDark) {
        Surface {
            SortingBottomSheetContents(onSortingTypeSelected = {})
        }
    }
}

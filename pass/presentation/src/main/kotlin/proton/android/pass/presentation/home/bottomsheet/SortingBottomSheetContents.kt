package proton.android.pass.presentation.home.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetTitle
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import me.proton.pass.presentation.R
import proton.android.pass.presentation.home.SortingType

@ExperimentalMaterialApi
@Composable
fun SortingBottomSheetContents(
    modifier: Modifier = Modifier,
    sortingType: SortingType = SortingType.ByName,
    onSortingTypeSelected: (SortingType) -> Unit
) {
    Column(modifier) {
        BottomSheetTitle(
            title = stringResource(id = R.string.sorting_bottomsheet_title),
            showDivider = false
        )
        BottomSheetItemList(
            items = sortingItemList(sortingType, onSortingTypeSelected)
        )
    }
}

private fun sortingItemList(
    selectedSortingType: SortingType,
    onSortingTypeSelected: (SortingType) -> Unit
): ImmutableList<BottomSheetItem> =
    SortingType.values()
        .map {
            object : BottomSheetItem {
                override val title: @Composable () -> Unit
                    get() = { BottomSheetItemTitle(text = stringResource(id = it.titleId)) }
                override val subtitle: @Composable (() -> Unit)?
                    get() = null
                override val icon: @Composable (() -> Unit)?
                    get() = if (it == selectedSortingType) {
                        { BottomSheetItemIcon(iconId = me.proton.core.presentation.R.drawable.ic_proton_checkmark) }
                    } else null
                override val onClick: () -> Unit
                    get() = { onSortingTypeSelected(it) }
            }
        }
        .toImmutableList()

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

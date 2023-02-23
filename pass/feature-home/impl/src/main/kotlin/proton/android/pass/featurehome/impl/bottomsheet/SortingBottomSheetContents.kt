package proton.android.pass.featurehome.impl.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.bottomSheetPadding
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetTitle
import proton.android.pass.featurehome.impl.R
import proton.android.pass.featurehome.impl.SortingType

@ExperimentalMaterialApi
@Composable
fun SortingBottomSheetContents(
    modifier: Modifier = Modifier,
    sortingType: SortingType = SortingType.ByName,
    onSortingTypeSelected: (SortingType) -> Unit
) {
    Column(
        modifier = modifier.bottomSheetPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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
    listOf(SortingType.ByModificationDate, SortingType.ByName, SortingType.ByItemType)
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
                override val isDivider = false
            }
        }
        .toImmutableList()

@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun SortingBottomSheetContentsPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            SortingBottomSheetContents(onSortingTypeSelected = {})
        }
    }
}

package proton.android.pass.featurehome.impl.bottomsheet

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.bottomSheetPadding
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.bottomSheetDivider
import proton.android.pass.featurehome.impl.SortingType

@ExperimentalMaterialApi
@Composable
fun SortingBottomSheetContents(
    modifier: Modifier = Modifier,
    sortingType: SortingType = SortingType.TitleAsc,
    onSortingTypeSelected: (SortingType) -> Unit
) {
    BottomSheetItemList(
        modifier = modifier.bottomSheetPadding(),
        items = sortingItemList(sortingType, onSortingTypeSelected)
            .flatMap { listOf(it, bottomSheetDivider()) }
            .dropLast(1)
            .toPersistentList()
    )
}

private fun sortingItemList(
    selectedSortingType: SortingType,
    onSortingTypeSelected: (SortingType) -> Unit
): ImmutableList<BottomSheetItem> =
    listOf(
        SortingType.MostRecent,
        SortingType.TitleAsc,
        SortingType.TitleDesc,
        SortingType.CreationAsc,
        SortingType.CreationDesc
    )
        .map {
            object : BottomSheetItem {
                override val title: @Composable () -> Unit
                    get() = {
                        val color = if (it == selectedSortingType) {
                            PassTheme.colors.accentBrandNorm
                        } else {
                            PassTheme.colors.textNorm
                        }
                        BottomSheetItemTitle(text = stringResource(id = it.titleId), color = color)
                    }
                override val subtitle: @Composable (() -> Unit)?
                    get() = null
                override val leftIcon: @Composable (() -> Unit)?
                    get() = null
                override val endIcon: @Composable (() -> Unit)?
                    get() = if (it == selectedSortingType) {
                        {
                            BottomSheetItemIcon(
                                iconId = me.proton.core.presentation.R.drawable.ic_proton_checkmark,
                                tint = PassTheme.colors.accentBrandOpaque
                            )
                        }
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

package proton.android.featuresearchoptions.impl

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
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.featuresearchoptions.api.SearchSortingType
import proton.android.pass.featuresearchoptions.api.SearchSortingType.CreationAsc
import proton.android.pass.featuresearchoptions.api.SearchSortingType.CreationDesc
import proton.android.pass.featuresearchoptions.api.SearchSortingType.MostRecent
import proton.android.pass.featuresearchoptions.api.SearchSortingType.TitleAsc
import proton.android.pass.featuresearchoptions.api.SearchSortingType.TitleDesc
import proton.android.pass.composecomponents.impl.R as CompR

@ExperimentalMaterialApi
@Composable
fun SortingBottomSheetContents(
    modifier: Modifier = Modifier,
    sortingType: SearchSortingType = TitleAsc,
    onSortingTypeSelected: (SearchSortingType) -> Unit
) {
    BottomSheetItemList(
        modifier = modifier.bottomSheet(),
        items = sortingItemList(sortingType, onSortingTypeSelected)
            .withDividers()
            .toPersistentList()
    )
}

private fun sortingItemList(
    selectedSortingType: SearchSortingType,
    onSortingTypeSelected: (SearchSortingType) -> Unit
): ImmutableList<BottomSheetItem> =
    listOf(MostRecent, TitleAsc, TitleDesc, CreationAsc, CreationDesc)
        .map {
            object : BottomSheetItem {
                override val title: @Composable () -> Unit
                    get() = {
                        val color = if (it == selectedSortingType) {
                            PassTheme.colors.interactionNorm
                        } else {
                            PassTheme.colors.textNorm
                        }
                        val title = when (it) {
                            MostRecent -> stringResource(id = CompR.string.sort_by_modification_date)
                            TitleAsc -> stringResource(id = CompR.string.sort_by_title_asc)
                            TitleDesc -> stringResource(id = CompR.string.sort_by_title_desc)
                            CreationAsc -> stringResource(id = CompR.string.sort_by_creation_asc)
                            CreationDesc -> stringResource(id = CompR.string.sort_by_creation_desc)
                        }
                        BottomSheetItemTitle(text = title, color = color)
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
                                tint = PassTheme.colors.interactionNormMajor1
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

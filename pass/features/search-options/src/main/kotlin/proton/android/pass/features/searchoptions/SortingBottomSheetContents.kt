/*
 * Copyright (c) 2023-2024 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.features.searchoptions

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
import proton.android.pass.searchoptions.api.SearchSortingType
import proton.android.pass.searchoptions.api.SearchSortingType.CreationAsc
import proton.android.pass.searchoptions.api.SearchSortingType.CreationDesc
import proton.android.pass.searchoptions.api.SearchSortingType.MostRecent
import proton.android.pass.searchoptions.api.SearchSortingType.TitleAsc
import proton.android.pass.searchoptions.api.SearchSortingType.TitleDesc
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
): ImmutableList<BottomSheetItem> = listOf(MostRecent, TitleAsc, TitleDesc, CreationAsc, CreationDesc)
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
fun SortingBottomSheetContentsPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            SortingBottomSheetContents(onSortingTypeSelected = {})
        }
    }
}

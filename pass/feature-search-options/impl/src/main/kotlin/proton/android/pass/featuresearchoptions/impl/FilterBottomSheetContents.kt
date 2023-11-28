/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.featuresearchoptions.impl

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.featuresearchoptions.api.SearchFilterType
import proton.android.pass.featuresearchoptions.api.SearchFilterType.Alias
import proton.android.pass.featuresearchoptions.api.SearchFilterType.All
import proton.android.pass.featuresearchoptions.api.SearchFilterType.CreditCard
import proton.android.pass.featuresearchoptions.api.SearchFilterType.Login
import proton.android.pass.featuresearchoptions.api.SearchFilterType.Note

@ExperimentalMaterialApi
@Composable
fun FilterBottomSheetContents(
    modifier: Modifier = Modifier,
    searchFilterType: SearchFilterType = All,
    onSortingTypeSelected: (SearchFilterType) -> Unit
) {
    BottomSheetItemList(
        modifier = modifier.bottomSheet(),
        items = filterItemList(searchFilterType, onSortingTypeSelected)
            .withDividers()
            .toPersistentList()
    )
}

private fun filterItemList(
    searchFilterType: SearchFilterType,
    onSortingTypeSelected: (SearchFilterType) -> Unit
): ImmutableList<BottomSheetItem> =
    listOf(All, Login, Alias, Note, CreditCard)
        .map {
            object : BottomSheetItem {
                override val title: @Composable () -> Unit
                    get() = {
                        val color = if (it == searchFilterType) {
                            PassTheme.colors.interactionNorm
                        } else {
                            PassTheme.colors.textNorm
                        }
                        val title = when (it) {
                            All -> stringResource(id = R.string.item_type_filter_all)
                            Login -> stringResource(id = R.string.item_type_filter_login)
                            Alias -> stringResource(id = R.string.item_type_filter_alias)
                            Note -> stringResource(id = R.string.item_type_filter_note)
                            CreditCard -> stringResource(id = R.string.item_type_filter_credit_card)
                        }
                        BottomSheetItemTitle(text = title, color = color)
                    }
                override val subtitle: @Composable (() -> Unit)?
                    get() = null
                override val leftIcon: @Composable (() -> Unit)?
                    get() = null
                override val endIcon: @Composable (() -> Unit)?
                    get() = if (it == searchFilterType) {
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

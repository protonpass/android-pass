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

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.toPersistentList
import me.proton.core.util.kotlin.toInt
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.data.api.ItemCountSummary
import proton.android.pass.searchoptions.api.FilterOption
import proton.android.pass.searchoptions.api.SearchFilterType
import proton.android.pass.searchoptions.api.SearchOptions
import proton.android.pass.searchoptions.api.SearchSortingType
import proton.android.pass.searchoptions.api.SortingOption
import proton.android.pass.searchoptions.api.VaultSelectionOption
import me.proton.core.presentation.R as CoreR

@Composable
internal fun FilterBottomSheetContents(
    modifier: Modifier = Modifier,
    state: FilterOptionsState.Success,
    onSortingTypeSelected: (SearchFilterType) -> Unit
) = with(state) {
    buildList {
        filterRow(
            titleResId = R.string.item_type_filter_all,
            startIconResId = CoreR.drawable.ic_proton_list_bullets,
            itemCount = summary.total,
            isSelected = filterType == SearchFilterType.All,
            onClick = { onSortingTypeSelected(SearchFilterType.All) }
        ).also(::add)

        filterRow(
            titleResId = R.string.item_type_filter_login,
            startIconResId = CoreR.drawable.ic_proton_user,
            itemCount = summary.login,
            isSelected = filterType == SearchFilterType.Login,
            onClick = { onSortingTypeSelected(SearchFilterType.Login) }
        ).also(::add)

        filterRow(
            titleResId = R.string.item_type_filter_alias,
            startIconResId = CoreR.drawable.ic_proton_alias,
            itemCount = summary.alias,
            isSelected = filterType == SearchFilterType.Alias,
            onClick = { onSortingTypeSelected(SearchFilterType.Alias) }
        ).also(::add)

        filterRow(
            titleResId = R.string.item_type_filter_note,
            startIconResId = CoreR.drawable.ic_proton_file_lines,
            itemCount = summary.note,
            isSelected = filterType == SearchFilterType.Note,
            onClick = { onSortingTypeSelected(SearchFilterType.Note) }
        ).also(::add)

        filterRow(
            titleResId = R.string.item_type_filter_credit_card,
            startIconResId = CoreR.drawable.ic_proton_credit_card,
            itemCount = summary.creditCard,
            isSelected = filterType == SearchFilterType.CreditCard,
            onClick = { onSortingTypeSelected(SearchFilterType.CreditCard) }
        ).also(::add)

        filterRow(
            titleResId = R.string.item_type_filter_identity,
            startIconResId = CoreR.drawable.ic_proton_card_identity,
            itemCount = summary.identities,
            isSelected = filterType == SearchFilterType.Identity,
            onClick = { onSortingTypeSelected(SearchFilterType.Identity) }
        ).also(::add)

        if (isCustomItemEnabled) {
            filterRow(
                titleResId = R.string.item_type_filter_custom_item,
                startIconResId = CoreR.drawable.ic_proton_wrench,
                itemCount = summary.custom,
                isSelected = filterType == SearchFilterType.Custom,
                onClick = { onSortingTypeSelected(SearchFilterType.Custom) }
            ).also(::add)
        }

        filterRow(
            titleResId = R.string.item_type_filter_login_mfa,
            startIconResId = CoreR.drawable.ic_proton_lock,
            itemCount = summary.loginWithMFA,
            isSelected = filterType == SearchFilterType.LoginMFA,
            onClick = { onSortingTypeSelected(SearchFilterType.LoginMFA) }
        ).also(::add)

        if (isSharedWithMeFilterAvailable) {
            filterRow(
                titleResId = R.string.item_type_filter_shared_with_me,
                startIconResId = CoreR.drawable.ic_proton_user_arrow_left,
                itemCount = summary.sharedWithMe,
                isSelected = filterType == SearchFilterType.SharedWithMe,
                onClick = { onSortingTypeSelected(SearchFilterType.SharedWithMe) }
            ).also(::add)
        }

        if (isSharedByMeFilterAvailable) {
            filterRow(
                titleResId = R.string.item_type_filter_shared_by_me,
                startIconResId = CoreR.drawable.ic_proton_user_arrow_right,
                itemCount = summary.sharedByMe,
                isSelected = filterType == SearchFilterType.SharedByMe,
                onClick = { onSortingTypeSelected(SearchFilterType.SharedByMe) }
            ).also(::add)
        }
    }
}.let { items ->
    BottomSheetItemList(
        modifier = modifier.bottomSheet(),
        items = items
            .withDividers()
            .toPersistentList()
    )
}

@Composable
private fun filterRow(
    @StringRes titleResId: Int,
    @DrawableRes startIconResId: Int,
    itemCount: Long,
    isSelected: Boolean,
    onClick: () -> Unit
): BottomSheetItem = object : BottomSheetItem {

    val color = if (isSelected) PassTheme.colors.interactionNorm else PassTheme.colors.textNorm

    override val title: @Composable () -> Unit = {
        BottomSheetItemTitle(
            text = "${stringResource(id = titleResId)} ($itemCount)",
            color = color
        )
    }

    override val subtitle: @Composable (() -> Unit)? = null

    override val leftIcon: @Composable () -> Unit = {
        BottomSheetItemIcon(
            iconId = startIconResId,
            tint = color
        )
    }

    override val endIcon: @Composable () -> Unit = {
        if (isSelected) {
            BottomSheetItemIcon(
                iconId = CoreR.drawable.ic_proton_checkmark,
                tint = PassTheme.colors.interactionNormMajor1
            )
        }
    }

    override val onClick: () -> Unit = onClick

    override val isDivider: Boolean = false

}

@[Preview Composable]
internal fun FilterBottomSheetContentsPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    val (isDark, showSharedItems) = input

    val sharedItemsCount = showSharedItems.toInt().toLong()

    PassTheme(isDark = isDark) {
        Surface {
            FilterBottomSheetContents(
                state = FilterOptionsState.Success(
                    searchOptions = SearchOptions(
                        filterOption = FilterOption(SearchFilterType.All),
                        sortingOption = SortingOption(SearchSortingType.MostRecent),
                        vaultSelectionOption = VaultSelectionOption.AllVaults,
                        userId = null
                    ),
                    summary = ItemCountSummary(
                        login = 0,
                        loginWithMFA = 0,
                        note = 0,
                        alias = 0,
                        creditCard = 0,
                        identities = 0,
                        custom = 0,
                        sharedWithMe = sharedItemsCount,
                        sharedByMe = sharedItemsCount,
                        trashed = 0,
                        sharedWithMeTrashed = 0
                    ),
                    isCustomItemEnabled = true
                ),
                onSortingTypeSelected = {}
            )
        }
    }
}

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
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemSubtitle
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.searchoptions.api.SearchFilterType
import proton.android.pass.searchoptions.api.SearchSortingType
import me.proton.core.presentation.R as CoreR
import proton.android.pass.composecomponents.impl.R as ComponentsR

@ExperimentalMaterialApi
@Composable
fun SearchOptionsBottomSheetContents(
    modifier: Modifier = Modifier,
    state: SearchOptionsUIState,
    onNavigateEvent: (SearchOptionsNavigation) -> Unit,
    onResetSearchOptions: () -> Unit
) {
    val items = mutableListOf<BottomSheetItem>()
    if (state.showBulkActionsOption) {
        items.add(selectItems(onNavigateEvent))
    }
    items.add(filtering(state, onNavigateEvent))
    items.add(sorting(state, onNavigateEvent))
    if (state.showResetAction) {
        items.add(resetSearchOptions(onResetSearchOptions))
    }
    BottomSheetItemList(
        modifier = modifier.bottomSheet(),
        items = items.withDividers().toPersistentList()
    )
}

private fun selectItems(onNavigateEvent: (SearchOptionsNavigation) -> Unit): BottomSheetItem =
    object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = { BottomSheetItemTitle(text = stringResource(R.string.select_items)) }
        override val subtitle: (@Composable () -> Unit) = {}
        override val leftIcon: (@Composable () -> Unit)
            get() = { BottomSheetItemIcon(iconId = CoreR.drawable.ic_proton_checkmark_circle) }
        override val endIcon: (@Composable () -> Unit)? = null
        override val onClick: () -> Unit
            get() = { onNavigateEvent(SearchOptionsNavigation.BulkActions) }
        override val isDivider = false
    }

private fun filtering(
    state: SearchOptionsUIState,
    onNavigateEvent: (SearchOptionsNavigation) -> Unit
): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = { BottomSheetItemTitle(text = stringResource(R.string.show)) }
    override val subtitle: (@Composable () -> Unit)
        get() = {
            val title = when (state.filterType) {
                SearchFilterType.All -> stringResource(id = R.string.item_type_filter_all)
                SearchFilterType.Login -> stringResource(id = R.string.item_type_filter_login)
                SearchFilterType.Alias -> stringResource(id = R.string.item_type_filter_alias)
                SearchFilterType.Note -> stringResource(id = R.string.item_type_filter_note)
                SearchFilterType.CreditCard -> stringResource(id = R.string.item_type_filter_credit_card)
                SearchFilterType.Identity -> stringResource(id = R.string.item_type_filter_identity)
                SearchFilterType.LoginMFA -> stringResource(id = R.string.item_type_filter_login_mfa)
                SearchFilterType.SharedWithMe -> stringResource(id = R.string.item_type_filter_shared_with_me)
                SearchFilterType.SharedByMe -> stringResource(id = R.string.item_type_filter_shared_by_me)
            }
            BottomSheetItemSubtitle(text = "$title (${state.count})")
        }
    override val leftIcon: (@Composable () -> Unit)
        get() = { BottomSheetItemIcon(iconId = CoreR.drawable.ic_proton_filter) }
    override val endIcon: (@Composable () -> Unit)?
        get() = null
    override val onClick: () -> Unit
        get() = { onNavigateEvent(SearchOptionsNavigation.Filter) }
    override val isDivider = false
}

private fun sorting(state: SearchOptionsUIState, onNavigateEvent: (SearchOptionsNavigation) -> Unit): BottomSheetItem =
    object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = { BottomSheetItemTitle(text = stringResource(R.string.sort_by)) }
        override val subtitle: (@Composable () -> Unit)
            get() = {
                val title = when (state.sortingType) {
                    SearchSortingType.MostRecent ->
                        stringResource(id = ComponentsR.string.sort_by_modification_date)

                    SearchSortingType.TitleAsc ->
                        stringResource(id = ComponentsR.string.sort_by_title_asc)

                    SearchSortingType.TitleDesc ->
                        stringResource(id = ComponentsR.string.sort_by_title_desc)

                    SearchSortingType.CreationAsc ->
                        stringResource(id = ComponentsR.string.sort_by_creation_asc)

                    SearchSortingType.CreationDesc ->
                        stringResource(id = ComponentsR.string.sort_by_creation_desc)
                }
                BottomSheetItemSubtitle(text = title)
            }
        override val leftIcon: (@Composable () -> Unit)
            get() = { BottomSheetItemIcon(iconId = CoreR.drawable.ic_proton_arrow_down_arrow_up) }
        override val endIcon: (@Composable () -> Unit)?
            get() = null
        override val onClick: () -> Unit
            get() = { onNavigateEvent(SearchOptionsNavigation.Sorting) }
        override val isDivider = false
    }

private fun resetSearchOptions(onResetSearchOptions: () -> Unit): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = { BottomSheetItemTitle(text = stringResource(R.string.reset_filters)) }
    override val subtitle: (@Composable () -> Unit)? = null
    override val leftIcon: (@Composable () -> Unit)
        get() = { BottomSheetItemIcon(iconId = CoreR.drawable.ic_proton_cross_circle) }
    override val endIcon: (@Composable () -> Unit)?
        get() = null
    override val onClick: () -> Unit
        get() = { onResetSearchOptions() }
    override val isDivider = false
}

@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun SearchOptionsBottomSheetContentsPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            SearchOptionsBottomSheetContents(
                state = SearchOptionsUIState(
                    filterType = SearchFilterType.All,
                    sortingType = SearchSortingType.TitleAsc,
                    count = 2,
                    showBulkActionsOption = true
                ),
                onNavigateEvent = {},
                onResetSearchOptions = {}
            )
        }
    }
}

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
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemSubtitle
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.featuresearchoptions.api.SearchSortingType
import me.proton.core.presentation.R as CoreR
import proton.android.pass.composecomponents.impl.R as ComponentsR

@ExperimentalMaterialApi
@Composable
fun SearchOptionsBottomSheetContents(
    modifier: Modifier = Modifier,
    state: SearchOptionsUIState,
    onNavigateEvent: (SearchOptionsNavigation) -> Unit
) {
    BottomSheetItemList(
        modifier = modifier.bottomSheet(),
        items = listOf(filtering(state, onNavigateEvent), sorting(state, onNavigateEvent))
            .toPersistentList()
    )
}

private fun filtering(
    state: SearchOptionsUIState,
    onNavigateEvent: (SearchOptionsNavigation) -> Unit
): BottomSheetItem =
    object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = { BottomSheetItemTitle(text = "Show") }
        override val subtitle: (@Composable () -> Unit)
            get() = {
                (state as? SuccessSearchOptionsUIState)?.let {
                    BottomSheetItemSubtitle(text = "All (154)")
                }
            }
        override val leftIcon: (@Composable () -> Unit)
            get() = { BottomSheetItemIcon(iconId = CoreR.drawable.ic_proton_filter) }
        override val endIcon: (@Composable () -> Unit)?
            get() = null
        override val onClick: () -> Unit
            get() = { onNavigateEvent(SearchOptionsNavigation.Filter) }
        override val isDivider = false
    }

private fun sorting(
    state: SearchOptionsUIState,
    onNavigateEvent: (SearchOptionsNavigation) -> Unit
): BottomSheetItem =
    object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = { BottomSheetItemTitle(text = stringResource(R.string.sort_by)) }
        override val subtitle: (@Composable () -> Unit)
            get() = {
                (state as? SuccessSearchOptionsUIState)?.let {
                    val title = when (it.sortingType) {
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
            }
        override val leftIcon: (@Composable () -> Unit)
            get() = { BottomSheetItemIcon(iconId = CoreR.drawable.ic_proton_arrow_down_arrow_up) }
        override val endIcon: (@Composable () -> Unit)?
            get() = null
        override val onClick: () -> Unit
            get() = { onNavigateEvent(SearchOptionsNavigation.Sorting) }
        override val isDivider = false
    }

/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.featureselectitem.navigation

import androidx.activity.compose.BackHandler
import androidx.navigation.NavGraphBuilder
import proton.android.pass.common.api.Option
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.featureselectitem.ui.SelectItemScreen
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable

object SelectItem : NavItem(baseRoute = "select/item", isTopLevel = true)

sealed class SelectItemState(
    val itemTypeFilter: ItemTypeFilter,
    val suggestionsTitle: String
) {
    sealed class Autofill(
        filter: ItemTypeFilter,
        title: String,
    ) : SelectItemState(filter, title) {
        data class Login(
            val title: String,
            val suggestionsPackageName: Option<String>,
            val suggestionsUrl: Option<String>
        ) : Autofill(ItemTypeFilter.Logins, title)

        data class CreditCard(
            val title: String,
        ) : Autofill(ItemTypeFilter.CreditCards, title)
    }
}

fun NavGraphBuilder.selectItemGraph(
    state: SelectItemState,
    onScreenShown: () -> Unit,
    onNavigate: (SelectItemNavigation) -> Unit
) {
    composable(SelectItem) {
        BackHandler { onNavigate(SelectItemNavigation.Cancel) }
        SelectItemScreen(
            state = state,
            onScreenShown = onScreenShown,
            onNavigate = onNavigate
        )
    }
}

sealed interface SelectItemNavigation {
    object AddItem : SelectItemNavigation
    data class ItemSelected(val item: ItemUiModel) : SelectItemNavigation
    object SortingBottomsheet : SelectItemNavigation
    data class ItemOptions(val shareId: ShareId, val itemId: ItemId) : SelectItemNavigation
    object Cancel : SelectItemNavigation
    object Upgrade : SelectItemNavigation
}

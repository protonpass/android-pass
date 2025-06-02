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

package proton.android.pass.features.selectitem.navigation

import androidx.activity.compose.BackHandler
import androidx.navigation.NavGraphBuilder
import me.proton.core.domain.entity.UserId
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.Suggestion
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.features.selectitem.ui.SelectItemScreen
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable

object SelectItem : NavItem(baseRoute = "select/item", isTopLevel = true)

sealed class SelectItemState(
    val itemTypeFilter: ItemTypeFilter,
    val suggestionsTitle: String,
    val showPinnedItems: Boolean,
    val showCreateButton: Boolean,
    val isPasswordCredentialCreation: Boolean = false
) {
    sealed class Autofill(
        filter: ItemTypeFilter,
        title: String
    ) : SelectItemState(filter, title, showPinnedItems = true, showCreateButton = true) {
        data class Login(
            val title: String,
            val suggestion: Suggestion
        ) : Autofill(ItemTypeFilter.Logins, title)

        data class CreditCard(
            val title: String
        ) : Autofill(ItemTypeFilter.CreditCards, title)

        data class Identity(
            val title: String
        ) : Autofill(ItemTypeFilter.Identity, title)
    }

    sealed class Passkey(
        title: String,
        showPinnedItems: Boolean,
        showCreateButton: Boolean
    ) : SelectItemState(
        itemTypeFilter = ItemTypeFilter.Logins,
        suggestionsTitle = title,
        showPinnedItems = showPinnedItems,
        showCreateButton = showCreateButton
    ) {

        data class Register(
            val title: String,
            val suggestion: Suggestion.Url
        ) : Passkey(title, showPinnedItems = true, showCreateButton = true)

        data class Select(
            val title: String
        ) : Passkey(title, showPinnedItems = false, showCreateButton = false)
    }

    sealed class Password(title: String, isPasswordCredentialCreation: Boolean) : SelectItemState(
        suggestionsTitle = title,
        isPasswordCredentialCreation = isPasswordCredentialCreation,
        itemTypeFilter = ItemTypeFilter.Logins,
        showPinnedItems = false,
        showCreateButton = false
    ) {

        data class Register(internal val title: String) : Password(
            title = title,
            isPasswordCredentialCreation = true
        )

        data class Select(
            internal val title: String,
            internal val suggestion: Suggestion
        ) : Password(
            title = title,
            isPasswordCredentialCreation = false
        )

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
    data object SelectAccount : SelectItemNavigation
    data object AddItem : SelectItemNavigation

    @JvmInline
    value class ItemSelected(val item: ItemUiModel) : SelectItemNavigation

    @JvmInline
    value class SuggestionSelected(val item: ItemUiModel) : SelectItemNavigation
    data object SortingBottomsheet : SelectItemNavigation
    data class ItemOptions(val userId: UserId, val shareId: ShareId, val itemId: ItemId) :
        SelectItemNavigation

    data object Cancel : SelectItemNavigation
    data object Upgrade : SelectItemNavigation
}

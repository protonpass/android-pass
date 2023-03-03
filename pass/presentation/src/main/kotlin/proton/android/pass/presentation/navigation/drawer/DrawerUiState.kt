/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */
package proton.android.pass.presentation.navigation.drawer

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import me.proton.core.user.domain.entity.User
import proton.android.pass.commonuimodels.api.ShareUiModelWithItemCount
import proton.android.pass.data.api.ItemCountSummary
import proton.pass.domain.ShareId

sealed class ItemTypeSection {
    abstract val shareId: ShareId?
}
sealed interface NavigationDrawerSection {
    data class AllItems(override val shareId: ShareId? = null) :
        ItemTypeSection(), NavigationDrawerSection

    data class Logins(override val shareId: ShareId? = null) :
        ItemTypeSection(), NavigationDrawerSection

    data class Aliases(override val shareId: ShareId? = null) :
        ItemTypeSection(), NavigationDrawerSection

    data class Notes(override val shareId: ShareId? = null) :
        ItemTypeSection(), NavigationDrawerSection

    object Settings : NavigationDrawerSection
    object Trash : NavigationDrawerSection
}

@Immutable
data class DrawerUiState(
    @StringRes val appNameResId: Int,
    val closeOnBackEnabled: Boolean = true,
    val closeOnActionEnabled: Boolean = true,
    val currentUser: User? = null,
    val selectedSection: NavigationDrawerSection? = null,
    val itemCountSummary: ItemCountSummary = ItemCountSummary.Initial,
    val shares: List<ShareUiModelWithItemCount> = emptyList(),
    val trashedItemCount: Long = 0
)

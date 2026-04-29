/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.features.username

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import proton.android.pass.features.username.bottomsheet.GenerateUsernameBottomSheet
import proton.android.pass.features.username.dialog.separator.usernameWordSeparatorDialog
import proton.android.pass.navigation.api.NavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavItemType
import proton.android.pass.navigation.api.bottomSheet

object GenerateUsernameBottomsheetMode : NavArgId {
    override val key: String = "mode"
    override val navType = NavType.StringType
}

enum class GenerateUsernameBottomsheetModeValue {
    CopyAndClose,
    CancelConfirm
}

object GenerateUsernameBottomsheet : NavItem(
    baseRoute = "username/create/bottomsheet",
    navArgIds = listOf(GenerateUsernameBottomsheetMode),
    navItemType = NavItemType.Bottomsheet
) {
    fun buildRoute(mode: GenerateUsernameBottomsheetModeValue) = "$baseRoute/${mode.name}"
}

sealed interface GenerateUsernameNavigation {

    data object DismissBottomsheet : GenerateUsernameNavigation

    data object CloseDialog : GenerateUsernameNavigation

    data object OnSelectWordSeparator : GenerateUsernameNavigation
}

fun NavGraphBuilder.generateUsernameBottomsheetGraph(onNavigate: (GenerateUsernameNavigation) -> Unit) {
    bottomSheet(GenerateUsernameBottomsheet) {
        GenerateUsernameBottomSheet(onNavigate = onNavigate)
    }

    usernameWordSeparatorDialog(onNavigate = onNavigate)
}

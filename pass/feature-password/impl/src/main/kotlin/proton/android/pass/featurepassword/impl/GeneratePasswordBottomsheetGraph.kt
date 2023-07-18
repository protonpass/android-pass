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

package proton.android.pass.featurepassword.impl

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import proton.android.pass.featurepassword.impl.bottomsheet.GeneratePasswordBottomSheet
import proton.android.pass.featurepassword.impl.dialog.mode.passwordModeDialog
import proton.android.pass.featurepassword.impl.dialog.separator.wordSeparatorDialog
import proton.android.pass.navigation.api.NavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavItemType
import proton.android.pass.navigation.api.bottomSheet

object GeneratePasswordBottomsheetMode : NavArgId {
    override val key: String = "mode"
    override val navType = NavType.StringType
}

enum class GeneratePasswordBottomsheetModeValue {
    CopyAndClose,
    CancelConfirm
}

object GeneratePasswordBottomsheet : NavItem(
    baseRoute = "password/create/bottomsheet",
    navArgIds = listOf(GeneratePasswordBottomsheetMode),
    navItemType = NavItemType.Bottomsheet
) {
    fun buildRoute(mode: GeneratePasswordBottomsheetModeValue) =
        "$baseRoute/${mode.name}"
}

sealed interface GeneratePasswordNavigation {
    object DismissBottomsheet : GeneratePasswordNavigation
    object CloseDialog : GeneratePasswordNavigation
    object OnSelectWordSeparator : GeneratePasswordNavigation
    object OnSelectPasswordMode : GeneratePasswordNavigation
}

fun NavGraphBuilder.generatePasswordBottomsheetGraph(
    onNavigate: (GeneratePasswordNavigation) -> Unit
) {
    bottomSheet(GeneratePasswordBottomsheet) {
        GeneratePasswordBottomSheet(onNavigate = onNavigate)
    }

    wordSeparatorDialog(onNavigate = onNavigate)
    passwordModeDialog(onNavigate = onNavigate)
}

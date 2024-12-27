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

package proton.android.pass.features.itemcreate.identity.navigation.customsection

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import proton.android.pass.features.itemcreate.bottomsheets.customfield.CustomFieldIndexNavArgId
import proton.android.pass.features.itemcreate.bottomsheets.customfield.CustomFieldTitleNavArgId
import proton.android.pass.features.itemcreate.identity.ui.customsection.CustomSectionNameDialog
import proton.android.pass.features.itemcreate.identity.ui.customsection.EditCustomSectionBottomSheet
import proton.android.pass.features.itemcreate.identity.ui.customsection.EditCustomSectionNameDialog
import proton.android.pass.navigation.api.NavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavItemType
import proton.android.pass.navigation.api.NavParamEncoder
import proton.android.pass.navigation.api.bottomSheet
import proton.android.pass.navigation.api.dialog

object CustomSectionIndexNavArgId : NavArgId {
    override val key = "index"
    override val navType = NavType.IntType
}

object CustomSectionTitleNavArgId : NavArgId {
    override val key = "title"
    override val navType = NavType.StringType
}

object CustomSectionNameDialogNavItem : NavItem(
    baseRoute = "item/create/customsection/add/dialog",
    navItemType = NavItemType.Dialog
)

object EditCustomSectionNameDialogNavItem : NavItem(
    baseRoute = "item/create/customsection/edit/dialog",
    navArgIds = listOf(CustomSectionIndexNavArgId, CustomSectionTitleNavArgId),
    navItemType = NavItemType.Dialog
) {
    fun buildRoute(index: Int, currentValue: String) = "$baseRoute/$index/${NavParamEncoder.encode(currentValue)}"
}

object CustomSectionOptionsBottomSheetNavItem : NavItem(
    baseRoute = "item/create/customsection/options/bottomsheet",
    navArgIds = listOf(CustomFieldIndexNavArgId, CustomFieldTitleNavArgId),
    navItemType = NavItemType.Bottomsheet
) {
    fun buildRoute(index: Int, currentTitle: String) = buildString {
        append("$baseRoute/$index/")
        val encodedTitle = NavParamEncoder.encode(currentTitle)
        if (encodedTitle.isNotBlank()) {
            append(encodedTitle)
        } else {
            append("Unknown")
        }
    }
}

fun NavGraphBuilder.extraSectionGraph(onNavigate: (ExtraSectionNavigation) -> Unit) {

    dialog(CustomSectionNameDialogNavItem) {
        CustomSectionNameDialog(onNavigate = onNavigate)
    }

    dialog(EditCustomSectionNameDialogNavItem) {
        EditCustomSectionNameDialog(onNavigate = onNavigate)
    }

    bottomSheet(CustomSectionOptionsBottomSheetNavItem) {
        EditCustomSectionBottomSheet(onNavigate = onNavigate)
    }
}

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

package proton.android.pass.featureitemcreate.impl.dialogs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import proton.android.pass.featureitemcreate.impl.bottomsheets.customfield.CustomFieldIndexNavArgId
import proton.android.pass.featureitemcreate.impl.bottomsheets.customfield.CustomFieldTitleNavArgId
import proton.android.pass.featureitemcreate.impl.bottomsheets.customfield.CustomFieldType
import proton.android.pass.navigation.api.NavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavItemType
import proton.android.pass.navigation.api.NavParamEncoder
import proton.android.pass.navigation.api.dialog

object CustomFieldTypeNavArgId : NavArgId {
    override val key = "customFieldType"
    override val navType = NavType.StringType
}

object CustomFieldNameDialog : NavItem(
    baseRoute = "item/create/customfield/add/dialog",
    navArgIds = listOf(CustomFieldTypeNavArgId),
    navItemType = NavItemType.Dialog
) {
    fun buildRoute(type: CustomFieldType) = "$baseRoute/${type.name}"
}

object EditCustomFieldNameDialog : NavItem(
    baseRoute = "item/create/customfield/edit/dialog",
    navArgIds = listOf(CustomFieldIndexNavArgId, CustomFieldTitleNavArgId),
    navItemType = NavItemType.Dialog
) {
    fun buildRoute(index: Int, currentValue: String) =
        "$baseRoute/$index/${NavParamEncoder.encode(currentValue)}"
}

sealed interface CustomFieldNameNavigation {
    object Close : CustomFieldNameNavigation
}

fun NavGraphBuilder.customFieldNameDialogGraph(
    onNavigate: (CustomFieldNameNavigation) -> Unit
) {
    dialog(CustomFieldNameDialog) {
        CustomFieldNameDialog(onNavigate = onNavigate)
    }

    dialog(EditCustomFieldNameDialog) {
        EditCustomFieldNameDialog(onNavigate = onNavigate)
    }
}

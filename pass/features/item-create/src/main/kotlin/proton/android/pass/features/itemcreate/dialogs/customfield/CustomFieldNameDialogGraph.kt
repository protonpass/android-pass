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

package proton.android.pass.features.itemcreate.dialogs.customfield

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.SpecialCharacters
import proton.android.pass.common.api.getOrElse
import proton.android.pass.domain.CustomFieldType
import proton.android.pass.features.itemcreate.bottomsheets.customfield.CustomFieldIndexNavArgId
import proton.android.pass.features.itemcreate.bottomsheets.customfield.CustomFieldTitleNavArgId
import proton.android.pass.features.itemcreate.common.CustomFieldPrefix
import proton.android.pass.features.itemcreate.common.customsection.CustomSectionIndexNavArgId
import proton.android.pass.navigation.api.NavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavItemType
import proton.android.pass.navigation.api.NavParamEncoder
import proton.android.pass.navigation.api.dialog

object CustomFieldTypeNavArgId : NavArgId {
    override val key = "customFieldType"
    override val navType = NavType.StringType
}

class CustomFieldNameDialogNavItem(prefix: CustomFieldPrefix) : NavItem(
    baseRoute = "${prefix.name}/item/create/customfield/add/dialog",
    navArgIds = listOf(CustomFieldTypeNavArgId, CustomSectionIndexNavArgId),
    navItemType = NavItemType.Dialog
) {
    fun buildRoute(type: CustomFieldType, sectionIndex: Option<Int> = None) = buildString {
        append(baseRoute)
        append(SpecialCharacters.SLASH)
        append(type.name)
        append(SpecialCharacters.SLASH)
        append(sectionIndex.getOrElse { -1 })
    }

    companion object {
        val CreateLogin = CustomFieldNameDialogNavItem(CustomFieldPrefix.CreateLogin)
        val CreateAlias = CustomFieldNameDialogNavItem(CustomFieldPrefix.CreateAlias)
        val CreateCreditCard = CustomFieldNameDialogNavItem(CustomFieldPrefix.CreateCreditCard)
        val CreateIdentity = CustomFieldNameDialogNavItem(CustomFieldPrefix.CreateIdentity)
    }
}

class EditCustomFieldNameDialogNavItem(val prefix: CustomFieldPrefix) : NavItem(
    baseRoute = "${prefix.name}/item/create/customfield/edit/dialog",
    navArgIds = listOf(CustomFieldIndexNavArgId, CustomSectionIndexNavArgId, CustomFieldTitleNavArgId),
    navItemType = NavItemType.Dialog
) {
    fun buildRoute(
        index: Int,
        sectionIndex: Option<Int> = None,
        currentValue: String
    ) = buildString {
        append(baseRoute)
        append(SpecialCharacters.SLASH)
        append(index)
        append(SpecialCharacters.SLASH)
        append(sectionIndex.getOrElse { -1 })
        append(SpecialCharacters.SLASH)
        append(NavParamEncoder.encode(currentValue))
    }

    companion object {
        val CreateLogin = EditCustomFieldNameDialogNavItem(CustomFieldPrefix.CreateLogin)
        val CreateAlias = EditCustomFieldNameDialogNavItem(CustomFieldPrefix.CreateAlias)
        val CreateCreditCard = EditCustomFieldNameDialogNavItem(CustomFieldPrefix.CreateCreditCard)
        val CreateIdentity = EditCustomFieldNameDialogNavItem(CustomFieldPrefix.CreateIdentity)
    }
}

sealed interface CustomFieldNameNavigation {
    data object CloseScreen : CustomFieldNameNavigation
}

fun NavGraphBuilder.customFieldNameDialogGraph(
    prefix: CustomFieldPrefix,
    onNavigate: (CustomFieldNameNavigation) -> Unit
) {
    dialog(CustomFieldNameDialogNavItem(prefix)) {
        CustomFieldNameDialog(onNavigate = onNavigate)
    }

    dialog(EditCustomFieldNameDialogNavItem(prefix)) {
        EditCustomFieldNameDialog(onNavigate = onNavigate)
    }
}

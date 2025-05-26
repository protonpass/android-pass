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

package proton.android.pass.features.itemcreate.bottomsheets.customfield

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import me.proton.core.util.kotlin.takeIfNotBlank
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.SpecialCharacters
import proton.android.pass.common.api.getOrElse
import proton.android.pass.domain.CustomFieldType
import proton.android.pass.features.itemcreate.common.CustomFieldPrefix
import proton.android.pass.features.itemcreate.common.customsection.CustomSectionIndexNavArgId
import proton.android.pass.navigation.api.NavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavItemType
import proton.android.pass.navigation.api.NavParamEncoder
import proton.android.pass.navigation.api.bottomSheet

object CustomFieldIndexNavArgId : NavArgId {
    override val key = "index"
    override val navType = NavType.IntType
}

object CustomFieldTitleNavArgId : NavArgId {
    override val key = "title"
    override val navType = NavType.StringType
}

class AddCustomFieldBottomSheetNavItem(val prefix: CustomFieldPrefix) : NavItem(
    baseRoute = "${prefix.name}/item/create/customfield/add/bottomsheet",
    navArgIds = listOf(CustomSectionIndexNavArgId),
    navItemType = NavItemType.Bottomsheet
) {
    fun buildRoute(sectionIndex: Option<Int> = None) = buildString {
        append(baseRoute)
        append(SpecialCharacters.SLASH)
        append(sectionIndex.getOrElse { -1 })
    }

    companion object {
        val CreateLogin = AddCustomFieldBottomSheetNavItem(CustomFieldPrefix.CreateLogin)
        val CreateIdentity = AddCustomFieldBottomSheetNavItem(CustomFieldPrefix.CreateIdentity)
    }
}

class CustomFieldOptionsBottomSheetNavItem(val prefix: CustomFieldPrefix) : NavItem(
    baseRoute = "${prefix.name}/item/create/customfield/options/bottomsheet",
    navArgIds = listOf(
        CustomFieldIndexNavArgId,
        CustomSectionIndexNavArgId,
        CustomFieldTitleNavArgId
    ),
    navItemType = NavItemType.Bottomsheet
) {
    fun buildRoute(
        index: Int,
        sectionIndex: Option<Int> = None,
        currentTitle: String
    ) = buildString {
        append(baseRoute)
        append(SpecialCharacters.SLASH)
        append(index)
        append(SpecialCharacters.SLASH)
        append(sectionIndex.getOrElse { -1 })
        append(SpecialCharacters.SLASH)
        append(NavParamEncoder.encode(currentTitle.takeIfNotBlank() ?: "Unknown"))
    }

    companion object {
        val CreateLogin = CustomFieldOptionsBottomSheetNavItem(CustomFieldPrefix.CreateLogin)
        val CreateIdentity = CustomFieldOptionsBottomSheetNavItem(CustomFieldPrefix.CreateIdentity)
    }
}

sealed interface AddCustomFieldNavigation {
    data object AddText : AddCustomFieldNavigation
    data object AddHidden : AddCustomFieldNavigation
    data object AddTotp : AddCustomFieldNavigation
    data object AddDate : AddCustomFieldNavigation
}

sealed interface CustomFieldOptionsNavigation {
    data object Close : CustomFieldOptionsNavigation
    data class EditCustomField(
        val index: Int,
        val title: String,
        val sectionIndex: Option<Int>
    ) : CustomFieldOptionsNavigation

    data object RemoveCustomField : CustomFieldOptionsNavigation
}

fun NavGraphBuilder.customFieldBottomSheetGraph(
    prefix: CustomFieldPrefix,
    onAddCustomFieldNavigate: (CustomFieldType, Option<Int>) -> Unit,
    onEditCustomFieldNavigate: (String, Int, Option<Int>) -> Unit,
    onRemoveCustomFieldNavigate: () -> Unit,
    onDismissBottomsheet: () -> Unit
) {
    bottomSheet(AddCustomFieldBottomSheetNavItem(prefix)) {
        AddCustomFieldBottomSheet(prefix = prefix) { event, sectionIndex ->
            when (event) {
                is AddCustomFieldNavigation.AddText ->
                    onAddCustomFieldNavigate(CustomFieldType.Text, sectionIndex)

                is AddCustomFieldNavigation.AddHidden ->
                    onAddCustomFieldNavigate(CustomFieldType.Hidden, sectionIndex)

                is AddCustomFieldNavigation.AddTotp ->
                    onAddCustomFieldNavigate(CustomFieldType.Totp, sectionIndex)

                is AddCustomFieldNavigation.AddDate ->
                    onAddCustomFieldNavigate(CustomFieldType.Date, sectionIndex)
            }
        }
    }

    bottomSheet(CustomFieldOptionsBottomSheetNavItem(prefix)) {
        EditCustomFieldBottomSheet(
            onNavigate = {
                when (it) {
                    is CustomFieldOptionsNavigation.EditCustomField ->
                        onEditCustomFieldNavigate(it.title, it.index, it.sectionIndex)

                    CustomFieldOptionsNavigation.RemoveCustomField -> onRemoveCustomFieldNavigate()
                    CustomFieldOptionsNavigation.Close -> onDismissBottomsheet()
                }
            }
        )
    }
}


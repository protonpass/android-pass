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

package proton.android.pass.featureitemcreate.impl.bottomsheets.customfield

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import proton.android.pass.featureitemcreate.impl.login.BaseLoginNavigation
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

object AddCustomFieldBottomSheet : NavItem(
    baseRoute = "item/create/customfield/add/bottomsheet",
    navItemType = NavItemType.Bottomsheet
)
object CustomFieldOptionsBottomSheet : NavItem(
    baseRoute = "item/create/customfield/options/bottomsheet",
    navArgIds = listOf(CustomFieldIndexNavArgId, CustomFieldTitleNavArgId),
    navItemType = NavItemType.Bottomsheet
) {
    fun buildRoute(index: Int, currentTitle: String) =
        "$baseRoute/$index/${NavParamEncoder.encode(currentTitle)}"
}

enum class CustomFieldType {
    Text,
    Hidden,
    Totp
}

sealed interface AddCustomFieldNavigation {
    object Close : AddCustomFieldNavigation
    object AddText : AddCustomFieldNavigation
    object AddHidden : AddCustomFieldNavigation
    object AddTotp : AddCustomFieldNavigation
}

sealed interface CustomFieldOptionsNavigation {
    object Close : CustomFieldOptionsNavigation
    data class EditCustomField(val index: Int, val title: String) : CustomFieldOptionsNavigation
    object RemoveCustomField : CustomFieldOptionsNavigation
}

fun NavGraphBuilder.customFieldBottomSheetGraph(
    onNavigate: (BaseLoginNavigation) -> Unit
) {
    bottomSheet(AddCustomFieldBottomSheet) {
        AddCustomFieldBottomSheet {
            when (it) {
                is AddCustomFieldNavigation.Close -> {
                    onNavigate(BaseLoginNavigation.Close)
                }
                is AddCustomFieldNavigation.AddText -> {
                    onNavigate(BaseLoginNavigation.CustomFieldTypeSelected(CustomFieldType.Text))
                }
                is AddCustomFieldNavigation.AddHidden -> {
                    onNavigate(BaseLoginNavigation.CustomFieldTypeSelected(CustomFieldType.Hidden))
                }
                is AddCustomFieldNavigation.AddTotp -> {
                    onNavigate(BaseLoginNavigation.CustomFieldTypeSelected(CustomFieldType.Totp))
                }
            }
        }
    }

    bottomSheet(CustomFieldOptionsBottomSheet) {
        EditCustomFieldBottomSheet(
            onNavigate = {
                when (it) {
                    is CustomFieldOptionsNavigation.EditCustomField -> {
                        onNavigate(BaseLoginNavigation.EditCustomField(it.title, it.index))
                    }
                    CustomFieldOptionsNavigation.RemoveCustomField -> {
                        onNavigate(BaseLoginNavigation.RemovedCustomField)
                    }
                    CustomFieldOptionsNavigation.Close -> {
                        onNavigate(BaseLoginNavigation.Close)
                    }
                }
            }
        )
    }
}

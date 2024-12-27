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

package proton.android.pass.features.itemcreate.identity.navigation.bottomsheets

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import proton.android.pass.common.api.Option
import proton.android.pass.features.itemcreate.identity.ui.bottomsheets.IdentityFieldsBottomSheet
import proton.android.pass.navigation.api.NavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavItemType
import proton.android.pass.navigation.api.bottomSheet

object IdentityFieldsSectionNavArgId : NavArgId {
    override val key: String = "identityFieldsSection"
    override val navType: NavType<*> = NavType.EnumType(AddIdentityFieldType::class.java)
}

object IdentitySectionIndexNavArgId : NavArgId {
    override val key: String = "identitySectionIndex"
    override val navType: NavType<*> = NavType.IntType
}

object IdentityFieldsBottomSheet : NavItem(
    baseRoute = "identity/create/fields/bottomsheet",
    navArgIds = listOf(IdentityFieldsSectionNavArgId, IdentitySectionIndexNavArgId),
    navItemType = NavItemType.Bottomsheet
) {
    fun createRoute(addIdentityFieldType: AddIdentityFieldType, sectionIndex: Option<Int>): String =
        "$baseRoute/$addIdentityFieldType/${sectionIndex.value() ?: 0}"
}

fun NavGraphBuilder.identityFieldsGraph(onNavigate: (IdentityFieldsNavigation) -> Unit) {
    bottomSheet(IdentityFieldsBottomSheet) {
        IdentityFieldsBottomSheet(
            onNavigate = onNavigate
        )
    }
}

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

package proton.android.pass.features.sharing.manage.itemmemberoptions.navigation

import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole
import proton.android.pass.features.sharing.manage.bottomsheet.MemberEmailArg
import proton.android.pass.features.sharing.manage.bottomsheet.MemberShareIdArg
import proton.android.pass.features.sharing.manage.bottomsheet.ShareRoleArg
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavItemType
import proton.android.pass.navigation.api.NavParamEncoder

object ManageItemMemberOptionsNavItem : NavItem(
    baseRoute = "sharing/manage/item/member/options",
    navArgIds = listOf(CommonNavArgId.ShareId, MemberShareIdArg, ShareRoleArg, MemberEmailArg),
    navItemType = NavItemType.Bottomsheet
) {

    fun createNavRoute(
        shareId: ShareId,
        memberShareId: ShareId,
        memberShareRole: ShareRole,
        memberEmail: String
    ) = "$baseRoute/${shareId.id}/${memberShareId.id}/${memberShareRole.value}/${NavParamEncoder.encode(memberEmail)}"

}

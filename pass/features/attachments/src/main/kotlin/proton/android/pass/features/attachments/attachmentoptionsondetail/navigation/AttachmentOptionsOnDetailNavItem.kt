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

package proton.android.pass.features.attachments.attachmentoptionsondetail.navigation

import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.AttachmentId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavItemType
import proton.android.pass.navigation.api.toPath

data object AttachmentOptionsOnDetailNavItem : NavItem(
    baseRoute = "attachmentoptionsondetail/bottomsheet",
    navItemType = NavItemType.Bottomsheet,
    optionalArgIds = listOf(
        CommonOptionalNavArgId.ShareId,
        CommonOptionalNavArgId.ItemId,
        CommonOptionalNavArgId.AttachmentId
    )
) {
    fun createNavRoute(
        shareId: ShareId,
        itemId: ItemId,
        attachmentId: AttachmentId
    ) = buildString {
        append(baseRoute)
        val params = mapOf(
            CommonOptionalNavArgId.ShareId.key to shareId.id,
            CommonOptionalNavArgId.ItemId.key to itemId.id,
            CommonOptionalNavArgId.AttachmentId.key to attachmentId.id
        )
        append(params.toPath())
    }
}

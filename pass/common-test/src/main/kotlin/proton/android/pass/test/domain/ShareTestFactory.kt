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

package proton.android.pass.test.domain

import me.proton.core.domain.entity.UserId
import proton.android.pass.domain.Share
import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareFlags
import proton.android.pass.domain.ShareIcon
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.SharePermission
import proton.android.pass.domain.SharePermissionFlag
import proton.android.pass.domain.ShareRole
import proton.android.pass.domain.VaultId
import proton.android.pass.test.StringTestFactory
import java.util.Date
import kotlin.random.Random

object ShareTestFactory {

    fun random(): Share = if (Random.nextBoolean()) {
        Vault.create()
    } else {
        Item.create()
    }

    object Item {

        fun create(
            id: String = StringTestFactory.randomString(),
            userId: String = StringTestFactory.randomString(),
            targetId: String = StringTestFactory.randomString(),
            permission: SharePermission = SharePermission(SharePermissionFlag.entries.random().value),
            vaultId: String = StringTestFactory.randomString(),
            expirationTime: Date? = Date(),
            createTime: Date = Date(),
            shareRole: ShareRole = ShareRole.Admin,
            isOwner: Boolean = Random.nextBoolean(),
            memberCount: Int = Random.nextInt(),
            shared: Boolean = Random.nextBoolean(),
            maxMembers: Int = Random.nextInt(),
            pendingInvites: Int = Random.nextInt(),
            newUserInvitesReady: Int = Random.nextInt(),
            canAutofill: Boolean = Random.nextBoolean()
        ): Share.Item = Share.Item(
            id = ShareId(id),
            userId = UserId(userId),
            targetId = targetId,
            permission = permission,
            vaultId = VaultId(vaultId),
            groupId = null,
            groupEmail = null,
            expirationTime = expirationTime,
            createTime = createTime,
            shareRole = shareRole,
            isOwner = isOwner,
            memberCount = memberCount,
            shared = shared,
            maxMembers = maxMembers,
            pendingInvites = pendingInvites,
            newUserInvitesReady = newUserInvitesReady,
            canAutofill = canAutofill,
            shareFlags = ShareFlags(0)
        )
    }

    object Vault {

        fun create(
            id: String = StringTestFactory.randomString(),
            userId: String = StringTestFactory.randomString(),
            targetId: String = StringTestFactory.randomString(),
            permission: SharePermission = SharePermission(SharePermissionFlag.Admin.value),
            vaultId: String = StringTestFactory.randomString(),
            expirationTime: Date? = Date(),
            createTime: Date = Date(),
            shareRole: ShareRole = ShareRole.Admin,
            isOwner: Boolean = Random.nextBoolean(),
            memberCount: Int = Random.nextInt(),
            shared: Boolean = Random.nextBoolean(),
            maxMembers: Int = Random.nextInt(),
            pendingInvites: Int = Random.nextInt(),
            newUserInvitesReady: Int = Random.nextInt(),
            canAutofill: Boolean = Random.nextBoolean(),
            name: String = StringTestFactory.randomString(),
            color: ShareColor = ShareColor.entries.random(),
            icon: ShareIcon = ShareIcon.entries.random()
        ): Share.Vault = Share.Vault(
            id = ShareId(id),
            userId = UserId(userId),
            targetId = targetId,
            permission = permission,
            vaultId = VaultId(vaultId),
            groupId = null,
            groupEmail = null,
            expirationTime = expirationTime,
            createTime = createTime,
            shareRole = shareRole,
            isOwner = isOwner,
            memberCount = memberCount,
            shared = shared,
            maxMembers = maxMembers,
            pendingInvites = pendingInvites,
            newUserInvitesReady = newUserInvitesReady,
            canAutofill = canAutofill,
            name = name,
            color = color,
            icon = icon,
            shareFlags = ShareFlags(0)
        )
    }

}

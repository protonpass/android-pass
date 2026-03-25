/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.data.impl.fakes.mother

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import proton.android.pass.data.impl.db.entities.ShareEntity
import proton.android.pass.domain.ShareRole
import proton.android.pass.domain.ShareType

object ShareEntityTestFactory {

    object Vault {

        fun create(
            id: String = "share-id",
            userId: String = "user-id",
            addressId: String = "address-id",
            vaultId: String? = null,
            groupId: String? = null,
            groupEmail: String? = null,
            targetId: String? = null,
            permission: Int = 1,
            expirationTime: Long? = null,
            createTime: Long = 0L,
            encryptedContent: EncryptedByteArray? = null,
            isActive: Boolean = true,
            owner: Boolean = true,
            shareRoleId: String = ShareRole.SHARE_ROLE_ADMIN,
            targetMembers: Int = 1,
            shared: Boolean = false,
            targetMaxMembers: Int = 10,
            pendingInvites: Int = 0,
            newUserInvitesReady: Int = 0,
            canAutofill: Boolean = false,
            flags: Int = 0
        ): ShareEntity {
            val resolvedVaultId = vaultId ?: "vault-$id"
            return ShareEntity(
                id = id,
                userId = userId,
                addressId = addressId,
                vaultId = resolvedVaultId,
                groupId = groupId,
                groupEmail = groupEmail,
                targetType = ShareType.Vault.value,
                targetId = targetId ?: resolvedVaultId,
                permission = permission,
                content = null,
                contentKeyRotation = null,
                contentFormatVersion = null,
                expirationTime = expirationTime,
                createTime = createTime,
                encryptedContent = encryptedContent,
                isActive = isActive,
                owner = owner,
                shareRoleId = shareRoleId,
                targetMembers = targetMembers,
                shared = shared,
                targetMaxMembers = targetMaxMembers,
                pendingInvites = pendingInvites,
                newUserInvitesReady = newUserInvitesReady,
                canAutofill = canAutofill,
                flags = flags
            )
        }
    }

    object Item {

        fun create(
            id: String = "share-id",
            userId: String = "user-id",
            addressId: String = "address-id",
            vaultId: String? = null,
            groupId: String? = null,
            groupEmail: String? = null,
            targetId: String? = null,
            permission: Int = 1,
            expirationTime: Long? = null,
            createTime: Long = 0L,
            encryptedContent: EncryptedByteArray? = null,
            isActive: Boolean = true,
            owner: Boolean = true,
            shareRoleId: String = ShareRole.SHARE_ROLE_ADMIN,
            targetMembers: Int = 1,
            shared: Boolean = false,
            targetMaxMembers: Int = 10,
            pendingInvites: Int = 0,
            newUserInvitesReady: Int = 0,
            canAutofill: Boolean = false,
            flags: Int = 0
        ): ShareEntity = ShareEntity(
            id = id,
            userId = userId,
            addressId = addressId,
            vaultId = vaultId ?: "vault-$id",
            groupId = groupId,
            groupEmail = groupEmail,
            targetType = ShareType.Item.value,
            targetId = targetId ?: id,
            permission = permission,
            content = null,
            contentKeyRotation = null,
            contentFormatVersion = null,
            expirationTime = expirationTime,
            createTime = createTime,
            encryptedContent = encryptedContent,
            isActive = isActive,
            owner = owner,
            shareRoleId = shareRoleId,
            targetMembers = targetMembers,
            shared = shared,
            targetMaxMembers = targetMaxMembers,
            pendingInvites = pendingInvites,
            newUserInvitesReady = newUserInvitesReady,
            canAutofill = canAutofill,
            flags = flags
        )
    }
}

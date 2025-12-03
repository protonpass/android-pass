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

package proton.android.pass.data.impl.extensions

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import proton.android.pass.data.impl.db.entities.ShareEntity
import proton.android.pass.data.impl.responses.ShareResponse

/**
 * Maps a [ShareResponse] to a [ShareEntity].
 *
 * @param userId The user ID associated with the share
 * @param addressId The address ID associated with the share
 * @param encryptedContent The encrypted content (null if encryption key not available)
 * @param isActive Whether the share is active (false if encryption key is inactive or not found)
 * @param groupEmail The group email associated with the share (optional)
 */
fun ShareResponse.toEntity(
    userId: String,
    addressId: String,
    encryptedContent: EncryptedByteArray? = null,
    isActive: Boolean = true,
    groupEmail: String? = null
): ShareEntity = ShareEntity(
    id = shareId,
    userId = userId,
    addressId = addressId,
    vaultId = vaultId,
    groupId = groupId,
    targetType = targetType,
    targetId = targetId,
    permission = permission,
    content = content,
    contentKeyRotation = contentKeyRotation,
    contentFormatVersion = contentFormatVersion,
    expirationTime = expirationTime,
    createTime = createTime,
    encryptedContent = encryptedContent,
    isActive = isActive,
    shareRoleId = shareRoleId,
    owner = owner,
    targetMembers = targetMembers,
    shared = shared,
    targetMaxMembers = targetMaxMembers,
    pendingInvites = pendingInvites,
    newUserInvitesReady = newUserInvitesReady,
    canAutofill = canAutofill,
    flags = flags,
    groupEmail = groupEmail
)


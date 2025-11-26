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

import proton.android.pass.data.api.repositories.UpdateShareEvent
import proton.android.pass.data.impl.responses.ShareResponse

fun UpdateShareEvent.toResponse(): ShareResponse = ShareResponse(
    shareId = shareId,
    vaultId = vaultId,
    addressId = addressId,
    groupId = groupId,
    targetType = targetType,
    targetId = targetId,
    permission = permission,
    content = content,
    contentKeyRotation = contentKeyRotation,
    contentFormatVersion = contentFormatVersion,
    shareRoleId = shareRoleId,
    targetMembers = targetMembers,
    owner = owner,
    shared = shared,
    expirationTime = expirationTime,
    createTime = createTime,
    targetMaxMembers = targetMaxMembers,
    newUserInvitesReady = newUserInvitesReady,
    pendingInvites = pendingInvites,
    canAutofill = canAutofill,
    flags = flags
)

fun ShareResponse.toDomain(groupEmail: String?): UpdateShareEvent = UpdateShareEvent(
    shareId = shareId,
    vaultId = vaultId,
    addressId = addressId,
    groupId = groupId,
    groupEmail = groupEmail,
    targetType = targetType,
    targetId = targetId,
    permission = permission,
    content = content,
    contentKeyRotation = contentKeyRotation,
    contentFormatVersion = contentFormatVersion,
    shareRoleId = shareRoleId,
    targetMembers = targetMembers,
    owner = owner,
    shared = shared,
    expirationTime = expirationTime,
    createTime = createTime,
    targetMaxMembers = targetMaxMembers,
    newUserInvitesReady = newUserInvitesReady,
    pendingInvites = pendingInvites,
    canAutofill = canAutofill,
    flags = flags
)

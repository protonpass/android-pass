/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.data.impl.crypto

import proton.android.pass.crypto.api.usecases.invites.EncryptedGroupInviteAcceptKey
import proton.android.pass.crypto.api.usecases.invites.EncryptedInviteAcceptKey
import proton.android.pass.crypto.api.usecases.invites.EncryptedInviteKey
import proton.android.pass.data.impl.db.entities.GroupInviteKeyEntity
import proton.android.pass.data.impl.db.entities.UserInviteKeyEntity
import proton.android.pass.data.impl.requests.invites.InviteKeyRotation
import proton.android.pass.data.impl.responses.invites.KeyApiModel

internal fun GroupInviteKeyEntity.toEncryptedInviteKey(): EncryptedInviteKey =
    EncryptedInviteKey(keyRotation = keyRotation, key = key)

internal fun UserInviteKeyEntity.toEncryptedInviteKey(): EncryptedInviteKey =
    EncryptedInviteKey(keyRotation = keyRotation, key = key)

internal fun KeyApiModel.toEncryptedInviteKey(): EncryptedInviteKey =
    EncryptedInviteKey(keyRotation = keyRotation, key = key)

internal fun EncryptedInviteAcceptKey.toInviteKeyRotation(): InviteKeyRotation =
    InviteKeyRotation(keyRotation = keyRotation, key = key)

internal fun EncryptedGroupInviteAcceptKey.toInviteKeyRotation(): InviteKeyRotation =
    InviteKeyRotation(keyRotation = keyRotation, key = key)


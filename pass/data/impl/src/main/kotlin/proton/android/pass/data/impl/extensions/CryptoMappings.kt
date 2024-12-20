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

import proton.android.pass.crypto.api.usecases.EncryptedCreateItem
import proton.android.pass.crypto.api.usecases.EncryptedCreateVault
import proton.android.pass.crypto.api.usecases.EncryptedItemKey
import proton.android.pass.crypto.api.usecases.EncryptedItemRevision
import proton.android.pass.crypto.api.usecases.EncryptedUpdateItemRequest
import proton.android.pass.crypto.api.usecases.EncryptedUpdateVaultRequest
import proton.android.pass.data.api.repositories.ItemRevision
import proton.android.pass.data.impl.requests.CreateItemRequest
import proton.android.pass.data.impl.requests.CreateVaultRequest
import proton.android.pass.data.impl.requests.UpdateItemRequest
import proton.android.pass.data.impl.requests.UpdateVaultRequest
import proton.android.pass.data.impl.responses.ItemLatestKeyResponse

fun EncryptedCreateVault.toRequest(): CreateVaultRequest = CreateVaultRequest(
    addressId = addressId,
    content = content,
    contentFormatVersion = contentFormatVersion,
    encryptedVaultKey = encryptedVaultKey
)

fun EncryptedCreateItem.toRequest(): CreateItemRequest = CreateItemRequest(
    keyRotation = keyRotation,
    contentFormatVersion = contentFormatVersion,
    content = content,
    itemKey = itemKey
)

fun EncryptedUpdateItemRequest.toRequest(): UpdateItemRequest = UpdateItemRequest(
    keyRotation = keyRotation,
    lastRevision = lastRevision,
    contentFormatVersion = contentFormatVersion,
    content = content
)

fun ItemRevision.toCrypto(): EncryptedItemRevision = EncryptedItemRevision(
    itemId = itemId,
    revision = revision,
    contentFormatVersion = contentFormatVersion,
    keyRotation = keyRotation,
    content = content,
    state = state,
    aliasEmail = aliasEmail,
    createTime = createTime,
    modifyTime = modifyTime,
    lastUseTime = lastUseTime,
    revisionTime = revisionTime,
    key = itemKey,
    isPinned = isPinned,
    flags = flags,
    shareCount = shareCount
)

fun ItemLatestKeyResponse.toCrypto(): EncryptedItemKey = EncryptedItemKey(
    key = key,
    keyRotation = keyRotation
)

fun EncryptedUpdateVaultRequest.toRequest(): UpdateVaultRequest = UpdateVaultRequest(
    content = content,
    contentFormatVersion = contentFormatVersion,
    keyRotation = keyRotation
)

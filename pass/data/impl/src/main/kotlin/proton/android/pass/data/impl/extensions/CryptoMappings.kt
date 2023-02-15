package proton.android.pass.data.impl.extensions

import proton.android.pass.crypto.api.usecases.EncryptedCreateItem
import proton.android.pass.crypto.api.usecases.EncryptedCreateVault
import proton.android.pass.crypto.api.usecases.EncryptedItemKey
import proton.android.pass.crypto.api.usecases.EncryptedItemRevision
import proton.android.pass.crypto.api.usecases.EncryptedUpdateItemRequest
import proton.android.pass.data.impl.requests.CreateItemRequest
import proton.android.pass.data.impl.requests.CreateVaultRequest
import proton.android.pass.data.impl.requests.UpdateItemRequest
import proton.android.pass.data.impl.responses.ItemLatestKeyResponse
import proton.android.pass.data.impl.responses.ItemRevision

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
    content = content,
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
    key = itemKey
)

fun ItemLatestKeyResponse.toCrypto(): EncryptedItemKey = EncryptedItemKey(
    key = key,
    keyRotation = keyRotation
)

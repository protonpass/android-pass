package proton.android.pass.data.impl.extensions

import proton.android.pass.crypto.api.usecases.EncryptedCreateItem
import proton.android.pass.crypto.api.usecases.EncryptedCreateVault
import proton.android.pass.crypto.api.usecases.EncryptedItemRevision
import proton.android.pass.crypto.api.usecases.EncryptedUpdateItemRequest
import proton.android.pass.data.impl.requests.CreateItemRequest
import proton.android.pass.data.impl.requests.CreateVaultRequest
import proton.android.pass.data.impl.requests.UpdateItemRequest
import proton.android.pass.data.impl.responses.ItemRevision

fun EncryptedCreateVault.toRequest(): CreateVaultRequest = CreateVaultRequest(
    addressId = addressId,
    content = content,
    contentFormatVersion = contentFormatVersion,
    encryptedVaultKey = encryptedVaultKey
)

fun EncryptedCreateItem.toRequest(): CreateItemRequest = CreateItemRequest(
    rotationId = rotationId,
    labels = labels,
    vaultKeyPacket = vaultKeyPacket,
    vaultKeyPacketSignature = vaultKeyPacketSignature,
    contentFormatVersion = contentFormatVersion,
    content = content,
    userSignature = userSignature,
    itemKeySignature = itemKeySignature
)

fun EncryptedUpdateItemRequest.toRequest(): UpdateItemRequest = UpdateItemRequest(
    rotationId = rotationId,
    lastRevision = lastRevision,
    contentFormatVersion = contentFormatVersion,
    content = content,
    userSignature = userSignature,
    itemKeySignature = itemKeySignature
)

fun ItemRevision.toCrypto(): EncryptedItemRevision = EncryptedItemRevision(
    itemId = itemId,
    revision = revision,
    contentFormatVersion = contentFormatVersion,
    rotationId = rotationId,
    content = content,
    userSignature = userSignature,
    itemKeySignature = itemKeySignature,
    state = state,
    signatureEmail = signatureEmail,
    aliasEmail = aliasEmail,
    labels = labels,
    createTime = createTime,
    modifyTime = modifyTime,
    lastUseTime = lastUseTime,
    revisionTime = revisionTime
)

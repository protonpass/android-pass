package proton.android.pass.data.impl.extensions

import proton.android.pass.crypto.api.usecases.EncryptedCreateItem
import proton.android.pass.crypto.api.usecases.EncryptedCreateVault
import proton.android.pass.crypto.api.usecases.EncryptedItemKeyData
import proton.android.pass.crypto.api.usecases.EncryptedItemRevision
import proton.android.pass.crypto.api.usecases.EncryptedShareResponse
import proton.android.pass.crypto.api.usecases.EncryptedUpdateItemRequest
import proton.android.pass.crypto.api.usecases.EncryptedVaultItemKeyResponse
import proton.android.pass.crypto.api.usecases.EncryptedVaultKeyData
import proton.android.pass.crypto.api.usecases.VaultKeyList
import proton.android.pass.data.api.repositories.VaultItemKeyList
import proton.android.pass.data.impl.remote.VaultItemKeyResponseList
import proton.android.pass.data.impl.requests.CreateItemRequest
import proton.android.pass.data.impl.requests.CreateVaultRequest
import proton.android.pass.data.impl.requests.UpdateItemRequest
import proton.android.pass.data.impl.responses.ItemKeyData
import proton.android.pass.data.impl.responses.ItemRevision
import proton.android.pass.data.impl.responses.ShareResponse
import proton.android.pass.data.impl.responses.VaultKeyData

fun EncryptedCreateVault.toRequest(): CreateVaultRequest = CreateVaultRequest(
    addressId = addressId,
    content = content,
    contentFormatVersion = contentFormatVersion,
    contentEncryptedAddressSignature = contentEncryptedAddressSignature,
    contentEncryptedVaultSignature = contentEncryptedVaultSignature,
    vaultKey = vaultKey,
    vaultKeyPassphrase = vaultKeyPassphrase,
    vaultKeySignature = vaultKeySignature,
    keyPacket = keyPacket,
    keyPacketSignature = keyPacketSignature,
    signingKey = signingKey,
    signingKeyPassphrase = signingKeyPassphrase,
    signingKeyPassphraseKeyPacket = signingKeyPassphraseKeyPacket,
    acceptanceSignature = acceptanceSignature,
    itemKey = itemKey,
    itemKeyPassphrase = itemKeyPassphrase,
    itemKeyPassphraseKeyPacket = itemKeyPassphraseKeyPacket,
    itemKeySignature = itemKeySignature
)

fun VaultKeyList.toVaultItemKeyList(): VaultItemKeyList = VaultItemKeyList(
    vaultKeyList = vaultKeyList,
    itemKeyList = itemKeyList
)

fun ShareResponse.toCrypto(): EncryptedShareResponse = EncryptedShareResponse(
    shareId = shareId,
    vaultId = vaultId,
    targetType = targetType,
    targetId = targetId,
    permission = permission,
    acceptanceSignature = acceptanceSignature,
    inviterEmail = inviterEmail,
    inviterAcceptanceSignature = inviterAcceptanceSignature,
    signingKey = signingKey,
    signingKeyPassphrase = signingKeyPassphrase,
    content = content,
    contentFormatVersion = contentFormatVersion,
    contentRotationId = contentRotationId,
    contentEncryptedAddressSignature = contentEncryptedAddressSignature,
    contentEncryptedVaultSignature = contentEncryptedVaultSignature,
    contentSignatureEmail = contentSignatureEmail,
    expirationTime = expirationTime,
    createTime = createTime
)

fun proton.android.pass.data.impl.responses.VaultKeyList.toCrypto(): EncryptedVaultItemKeyResponse =
    EncryptedVaultItemKeyResponse(
        vaultKeys = vaultKeys.map { it.toCrypto() },
        itemKeys = itemKeys.map { it.toCrypto() }
    )

fun VaultKeyData.toCrypto(): EncryptedVaultKeyData = EncryptedVaultKeyData(
    rotationId = rotationId,
    rotation = rotation,
    key = key,
    keyPassphrase = keyPassphrase,
    keySignature = keySignature,
    createTime = createTime
)

fun ItemKeyData.toCrypto(): EncryptedItemKeyData = EncryptedItemKeyData(
    rotationId = rotationId,
    key = key,
    keyPassphrase = keyPassphrase,
    keySignature = keySignature,
    createTime = createTime
)

fun VaultItemKeyResponseList.toCrypto(): EncryptedVaultItemKeyResponse =
    EncryptedVaultItemKeyResponse(
        vaultKeys = vaultKeys.map { it.toCrypto() },
        itemKeys = itemKeys.map { it.toCrypto() }
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

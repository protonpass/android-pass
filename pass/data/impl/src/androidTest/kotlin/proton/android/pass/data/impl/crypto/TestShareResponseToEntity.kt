package proton.android.pass.data.impl.crypto

import me.proton.core.key.domain.entity.key.ArmoredKey
import me.proton.core.key.domain.entity.key.PublicKey
import org.junit.Test
import proton.android.pass.crypto.fakes.usecases.TestVerifyAcceptanceSignature
import proton.android.pass.crypto.fakes.usecases.TestVerifyShareContentSignatures
import proton.android.pass.crypto.fakes.utils.TestUtils
import proton.android.pass.data.impl.crypto.TestData.USER_PASSPHRASE
import proton.android.pass.data.impl.crypto.TestData.USER_PRIVATE_KEY
import proton.android.pass.data.impl.crypto.TestData.createVaultResponse
import proton.android.pass.data.impl.crypto.TestData.getVaultKey
import proton.pass.domain.key.VaultKey
import kotlin.test.assertEquals

class TestShareResponseToEntity {

    private val instance = ShareResponseToEntityImpl(
        TestVerifyAcceptanceSignature(),
        TestVerifyShareContentSignatures()
    )

    @Test
    fun testResponseToEntity() {
        val userAddress = TestUtils.createUserAddress(
            TestUtils.cryptoContext,
            key = USER_PRIVATE_KEY,
            passphrase = USER_PASSPHRASE.encodeToByteArray()
        )
        val userPublicKey = PublicKey(
            TestUtils.cryptoContext.pgpCrypto.getPublicKey(USER_PRIVATE_KEY),
            isPrimary = true,
            isActive = true,
            canEncrypt = true,
            canVerify = true
        )
        val vaultKey = getVaultKey()
        val vaultKeyInstance = VaultKey(
            rotationId = createVaultResponse.contentRotationId!!,
            rotation = 1,
            key = ArmoredKey.Private(vaultKey.key, vaultKey),
            encryptedKeyPassphrase = vaultKey.passphrase
        )

        val entity = instance.invoke(
            response = createVaultResponse,
            userAddress = userAddress,
            inviterKeys = listOf(userPublicKey),
            contentSignatureKeys = listOf(userPublicKey),
            vaultKeys = listOf(vaultKeyInstance),
        )

        assertEquals(entity.id, createVaultResponse.shareId)
        assertEquals(entity.userId, userAddress.userId.id)
        assertEquals(entity.addressId, userAddress.addressId.id)
        assertEquals(entity.vaultId, createVaultResponse.vaultId)
        assertEquals(entity.targetType, createVaultResponse.targetType)
        assertEquals(entity.targetId, createVaultResponse.targetId)
        assertEquals(entity.permission, createVaultResponse.permission)
        assertEquals(entity.inviterEmail, createVaultResponse.inviterEmail)
        assertEquals(entity.acceptanceSignature, createVaultResponse.acceptanceSignature)
        assertEquals(entity.inviterAcceptanceSignature, createVaultResponse.inviterAcceptanceSignature)
        assertEquals(entity.signingKey, createVaultResponse.signingKey)
        assertEquals(entity.signingKeyPassphrase, createVaultResponse.signingKeyPassphrase)
        assertEquals(entity.content, createVaultResponse.content)
        assertEquals(entity.contentFormatVersion, createVaultResponse.contentFormatVersion)
        assertEquals(entity.contentEncryptedAddressSignature, createVaultResponse.contentEncryptedAddressSignature)
        assertEquals(entity.contentEncryptedVaultSignature, createVaultResponse.contentEncryptedVaultSignature)
        assertEquals(entity.contentSignatureEmail, createVaultResponse.contentSignatureEmail)
        assertEquals(entity.nameKeyId, createVaultResponse.contentRotationId)
        assertEquals(entity.expirationTime, createVaultResponse.expirationTime)
        assertEquals(entity.createTime, createVaultResponse.createTime)
    }

}

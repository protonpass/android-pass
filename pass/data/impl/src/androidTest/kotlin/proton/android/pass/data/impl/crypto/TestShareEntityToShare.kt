package proton.android.pass.data.impl.crypto

import kotlinx.coroutines.test.runTest
import proton.android.pass.crypto.fakes.usecases.TestReadKey
import proton.android.pass.crypto.fakes.usecases.TestVerifyAcceptanceSignature
import proton.android.pass.crypto.fakes.utils.TestUtils
import proton.android.pass.data.fakes.repositories.TestVaultKeyRepository
import proton.android.pass.data.impl.db.entities.ShareEntity
import me.proton.core.key.domain.entity.key.ArmoredKey
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.user.domain.entity.UserAddress
import proton.android.pass.common.api.Result
import proton.pass.domain.SharePermissionFlag
import proton.pass.domain.flags
import proton.pass.domain.hasFlag
import proton.pass.domain.key.VaultKey
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestShareEntityToShare {

    private lateinit var instance: ShareEntityToShareImpl

    @Before
    fun setup() {

        val vaultKey = TestData.getVaultKey()

        val vaultKeyRepository = TestVaultKeyRepository()
        vaultKeyRepository.setVaultKeys(
            Result.Success(listOf(
            VaultKey(
                rotationId = TestData.createVaultResponse.contentRotationId!!,
                rotation = 1,
                key = ArmoredKey.Private(vaultKey.key, vaultKey),
                encryptedKeyPassphrase = vaultKey.passphrase
            )
        )))

        instance = ShareEntityToShareImpl(
            vaultKeyRepository = vaultKeyRepository,
            verifyAcceptanceSignature = TestVerifyAcceptanceSignature(),
            readKey = TestReadKey(
                ArmoredKey.Private(
                    armored = TestData.createVaultResponse.signingKey,
                    key = PrivateKey(
                        key = TestData.createVaultResponse.signingKey,
                        isPrimary = true,
                        isActive = true,
                        canEncrypt = true,
                        canVerify = true,
                        passphrase = null
                    )
                )
            )
        )
    }

    @Test
    fun testConversion() = runTest{
        val userAddress = TestUtils.createUserAddress(
            TestUtils.cryptoContext,
            key = TestData.USER_PRIVATE_KEY,
            passphrase = TestData.USER_PASSPHRASE.encodeToByteArray()
        )
        val userPublicKey = PublicKey(
            TestUtils.cryptoContext.pgpCrypto.getPublicKey(TestData.USER_PRIVATE_KEY),
            isPrimary = true,
            isActive = true,
            canEncrypt = true,
            canVerify = true
        )
        val res = instance.invoke(
            userAddress = userAddress,
            inviterKeys = listOf(userPublicKey),
            entity = getEntity(userAddress)
        )

        assertTrue(res is Result.Success)

        val share = res.data
        assertTrue(share.permission.hasFlag(SharePermissionFlag.Admin))

        val flags = share.permission.flags()
        assertEquals(1, flags.size)
    }

    fun getEntity(userAddress: UserAddress) = ShareEntity(
        id = TestData.createVaultResponse.shareId,
        userId = userAddress.userId.id,
        addressId = userAddress.addressId.id,
        vaultId = TestData.createVaultResponse.vaultId,
        targetType = TestData.createVaultResponse.targetType,
        targetId = TestData.createVaultResponse.targetId,
        permission = TestData.createVaultResponse.permission,
        inviterEmail = TestData.createVaultResponse.inviterEmail,
        acceptanceSignature = TestData.createVaultResponse.acceptanceSignature,
        inviterAcceptanceSignature = TestData.createVaultResponse.inviterAcceptanceSignature,
        signingKey = TestData.createVaultResponse.signingKey,
        signingKeyPassphrase = TestData.createVaultResponse.signingKeyPassphrase,
        content = TestData.createVaultResponse.content,
        contentFormatVersion = TestData.createVaultResponse.contentFormatVersion,
        contentEncryptedAddressSignature = TestData.createVaultResponse.contentEncryptedAddressSignature,
        contentEncryptedVaultSignature = TestData.createVaultResponse.contentEncryptedVaultSignature,
        contentSignatureEmail = TestData.createVaultResponse.contentSignatureEmail,
        nameKeyId = TestData.createVaultResponse.contentRotationId,
        expirationTime = TestData.createVaultResponse.expirationTime,
        createTime = TestData.createVaultResponse.createTime,

        keystoreEncryptedContent = null,
        keystoreEncryptedPassphrase = null
    )

}

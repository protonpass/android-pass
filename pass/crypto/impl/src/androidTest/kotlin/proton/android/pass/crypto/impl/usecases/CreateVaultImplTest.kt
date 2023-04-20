package proton.android.pass.crypto.impl.usecases

import me.proton.core.crypto.android.context.AndroidCryptoContext
import me.proton.core.crypto.android.pgp.GOpenPGPCrypto
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.pgp.VerificationStatus
import me.proton.core.key.domain.decryptAndVerifyData
import me.proton.core.key.domain.useKeys
import me.proton.core.user.domain.entity.User
import org.junit.Test
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionTag
import proton.android.pass.crypto.api.usecases.EncryptedCreateVault
import proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider
import proton.android.pass.crypto.fakes.utils.TestUtils
import proton.android.pass.test.crypto.TestKeyStoreCrypto
import proton_pass_vault_v1.VaultV1
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class CreateVaultImplTest {
    private val cryptoContext: CryptoContext = AndroidCryptoContext(
        keyStoreCrypto = TestKeyStoreCrypto,
        pgpCrypto = GOpenPGPCrypto(),
    )

    private val encryptionContextProvider = TestEncryptionContextProvider()

    @Test
    fun canCreateVault() {
        val userAddress = TestUtils.createUserAddress(cryptoContext)
        val vaultName = Utils.generatePassphrase()
        val vaultDescription = Utils.generatePassphrase()
        val user = TestUtils.createUser()
        val vaultMetadata = VaultV1.Vault.newBuilder()
            .setName(vaultName)
            .setDescription(vaultDescription)
            .build()

        val instance = CreateVaultImpl(cryptoContext, encryptionContextProvider)
        val (request, vaultKey) = instance.createVaultRequest(user, userAddress, vaultMetadata)

        validateVaultKey(user, request, vaultKey)
        validateVaultMetadata(vaultMetadata, request, vaultKey)
    }

    private fun validateVaultKey(
        user: User,
        request: EncryptedCreateVault,
        vaultKey: EncryptionKey,
    ) {
        val decryptedVaultKey = user.useKeys(cryptoContext) {
            val decoded = cryptoContext.pgpCrypto.getBase64Decoded(request.encryptedVaultKey)
            val armored = cryptoContext.pgpCrypto.getArmored(decoded)
            decryptAndVerifyData(armored)
        }

        assertEquals(decryptedVaultKey.status, VerificationStatus.Success)
        assertContentEquals(vaultKey.value(), decryptedVaultKey.data)
    }

    private fun validateVaultMetadata(
        metadata: VaultV1.Vault,
        request: EncryptedCreateVault,
        vaultKey: EncryptionKey
    ) {
        val decodedRequestContent = cryptoContext.pgpCrypto.getBase64Decoded(request.content)
        val decrypted = encryptionContextProvider.withEncryptionContext(vaultKey) {
            decrypt(EncryptedByteArray(decodedRequestContent), EncryptionTag.VaultContent)
        }

        val parsed = VaultV1.Vault.parseFrom(decrypted)
        assertEquals(metadata.name, parsed.name)
        assertEquals(metadata.description, parsed.description)
    }
}


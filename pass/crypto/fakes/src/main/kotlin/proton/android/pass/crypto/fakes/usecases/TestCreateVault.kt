package proton.android.pass.crypto.fakes.usecases

import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.util.kotlin.random
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.usecases.CreateVault
import proton.android.pass.crypto.api.usecases.CreateVaultOutput
import proton.android.pass.crypto.api.usecases.EncryptedCreateVault
import proton_pass_vault_v1.VaultV1

class TestCreateVault : CreateVault {

    private var result: Result<CreateVaultOutput> = Result.failure(IllegalStateException("result not set"))

    fun setResult(value: Result<CreateVaultOutput>) {
        result = value
    }

    override fun createVaultRequest(
        user: User,
        userAddress: UserAddress,
        vaultMetadata: VaultV1.Vault
    ): CreateVaultOutput = result.getOrThrow()

    companion object {
        fun generateOutput(): CreateVaultOutput = CreateVaultOutput(
            request = EncryptedCreateVault(
                addressId = String.Companion.random(),
                content = String.Companion.random(),
                contentFormatVersion = 1,
                encryptedVaultKey = String.Companion.random()
            ),
            shareKey = EncryptionKey.generate()
        )
    }
}

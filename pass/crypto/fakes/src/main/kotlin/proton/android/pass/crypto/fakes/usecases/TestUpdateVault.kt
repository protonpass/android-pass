package proton.android.pass.crypto.fakes.usecases

import me.proton.core.util.kotlin.random
import proton.android.pass.crypto.api.usecases.EncryptedUpdateVaultRequest
import proton.android.pass.crypto.api.usecases.UpdateVault
import proton.pass.domain.key.ShareKey
import proton_pass_vault_v1.VaultV1

class TestUpdateVault : UpdateVault {

    private var result: Result<EncryptedUpdateVaultRequest> = Result.failure(IllegalStateException("result not set"))

    fun setResult(value: Result<EncryptedUpdateVaultRequest>) {
        result = value
    }

    override fun createUpdateVaultRequest(
        shareKey: ShareKey,
        body: VaultV1.Vault
    ): EncryptedUpdateVaultRequest = result.getOrThrow()

    companion object {
        fun generateOutput(): EncryptedUpdateVaultRequest = EncryptedUpdateVaultRequest(
            content = String.Companion.random(),
            contentFormatVersion = 1,
            keyRotation = 1
        )
    }
}

package proton.android.pass.crypto.api.extensions

import proton.android.pass.common.api.Option
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.pass.domain.Share
import proton.pass.domain.Vault
import proton_pass_vault_v1.VaultV1

fun Share.toVault(encryptionContextProvider: EncryptionContextProvider): Option<Vault> = content
    .map {
        val decrypted = encryptionContextProvider.withEncryptionContext {
            decrypt(it)
        }
        val parsed = VaultV1.Vault.parseFrom(decrypted)
        Vault(shareId = id, isPrimary = isPrimary, name = parsed.name, color = color, icon = icon)
    }


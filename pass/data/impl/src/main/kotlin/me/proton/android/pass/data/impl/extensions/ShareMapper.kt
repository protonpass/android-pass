package me.proton.android.pass.data.impl.extensions

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.pass.domain.Share
import me.proton.pass.domain.ShareType
import proton_pass_vault_v1.VaultV1

fun Share.shareName(cryptoContext: CryptoContext): String {
    if (content == null) return "---"
    val decryptedContents = content!!.decrypt(cryptoContext.keyStoreCrypto)
    return when (shareType) {
        ShareType.Vault -> {
            val decoded = VaultV1.Vault.parseFrom(decryptedContents.array)
            decoded.name
        }
        else -> "Not supported"
    }
}

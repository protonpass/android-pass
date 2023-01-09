package me.proton.android.pass.crypto.api.usecases

import me.proton.core.key.domain.entity.key.PrivateKey

interface OpenShareContents {
    fun openVaultShareContents(encryptedShareContent: String, vaultKey: PrivateKey): ByteArray
}

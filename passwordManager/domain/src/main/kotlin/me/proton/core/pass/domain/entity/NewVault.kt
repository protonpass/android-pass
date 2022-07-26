package me.proton.core.pass.domain.entity

import me.proton.core.crypto.common.keystore.EncryptedString

data class NewVault(
    val name: EncryptedString,
    val description: EncryptedString
)

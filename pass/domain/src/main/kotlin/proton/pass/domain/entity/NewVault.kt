package proton.pass.domain.entity

import me.proton.core.crypto.common.keystore.EncryptedString
import proton.pass.domain.ShareColor
import proton.pass.domain.ShareIcon

data class NewVault(
    val name: EncryptedString,
    val description: EncryptedString,
    val icon: ShareIcon,
    val color: ShareColor
)

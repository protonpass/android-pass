package proton.android.pass.data.impl.extensions

import proton.android.pass.crypto.api.context.EncryptionContext
import proton.pass.domain.Item
import proton.pass.domain.ItemType

fun Item.hasTotp(encryptionContext: EncryptionContext): Boolean =
    when (val type = itemType) {
        is ItemType.Login -> {
            val totp = encryptionContext.decrypt(type.primaryTotp)
            totp.isNotBlank()
        }
        else -> false
    }

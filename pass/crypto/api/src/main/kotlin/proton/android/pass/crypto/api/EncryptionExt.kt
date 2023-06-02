package proton.android.pass.crypto.api

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString

fun EncryptedString.toEncryptedByteArray(): EncryptedByteArray =
    EncryptedByteArray(Base64.decodeBase64(this))

package proton.android.pass.featureitemdetail.impl.login

import androidx.compose.runtime.Stable
import me.proton.core.crypto.common.keystore.EncryptedString

@Stable
sealed class PasswordState(open val encrypted: EncryptedString) {

    @Stable
    data class Concealed(override val encrypted: EncryptedString) : PasswordState(encrypted)

    @Stable
    data class Revealed(
        override val encrypted: EncryptedString,
        val clearText: String
    ) : PasswordState(encrypted)
}

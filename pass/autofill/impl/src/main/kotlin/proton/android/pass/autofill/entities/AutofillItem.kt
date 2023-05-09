package proton.android.pass.autofill.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import me.proton.core.crypto.common.keystore.EncryptedString

@Parcelize
data class AutofillItem(
    val itemId: String,
    val shareId: String,
    val username: String,
    val password: EncryptedString?,
    val totp: EncryptedString?
) : Parcelable

package proton.android.pass.datamodels.api

import proton.android.pass.crypto.api.context.EncryptionContext
import proton.pass.domain.CustomField
import proton.pass.domain.CustomFieldContent

fun CustomField.toContent(encryptionContext: EncryptionContext): CustomFieldContent? = when (this) {
    CustomField.Unknown -> null
    is CustomField.Hidden -> {
        val decryptedValue = encryptionContext.decrypt(this.value)
        CustomFieldContent.Hidden(label = this.label, value = decryptedValue)
    }

    is CustomField.Text -> {
        CustomFieldContent.Text(label = this.label, value = this.value)
    }

    is CustomField.Totp -> {
        val decryptedValue = encryptionContext.decrypt(this.value)
        CustomFieldContent.Totp(label = this.label, value = decryptedValue)
    }
}

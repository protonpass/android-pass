package proton.android.pass.datamodels.api

import proton.android.pass.crypto.api.context.EncryptionContext
import proton.pass.domain.CustomField
import proton.pass.domain.CustomFieldContent
import proton.pass.domain.HiddenState

fun CustomField.toContent(
    encryptionContext: EncryptionContext,
    isConcealed: Boolean
): CustomFieldContent? = when (this) {
    CustomField.Unknown -> null
    is CustomField.Hidden -> {
        val value = if (isConcealed) {
            HiddenState.Concealed(this.value)
        } else {
            HiddenState.Revealed(this.value, encryptionContext.decrypt(this.value))
        }
        CustomFieldContent.Hidden(label = this.label, value = value)
    }

    is CustomField.Text -> {
        CustomFieldContent.Text(label = this.label, value = this.value)
    }

    is CustomField.Totp -> {
        val value = if (isConcealed) {
            HiddenState.Concealed(this.value)
        } else {
            HiddenState.Revealed(this.value, encryptionContext.decrypt(this.value))
        }
        CustomFieldContent.Totp(label = this.label, value = value)
    }
}

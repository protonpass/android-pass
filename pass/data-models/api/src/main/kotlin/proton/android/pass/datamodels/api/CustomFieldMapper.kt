package proton.android.pass.datamodels.api

import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.toEncryptedByteArray
import proton.pass.domain.CustomField
import proton.pass.domain.CustomFieldContent
import proton.pass.domain.HiddenState

fun CustomField.toContent(
    encryptionContext: EncryptionContext,
    isConcealed: Boolean
): CustomFieldContent? = when (this) {
    CustomField.Unknown -> null
    is CustomField.Hidden -> {
        val hiddenFieldByteArray = encryptionContext.decrypt(value.toEncryptedByteArray())
        val hiddenState = if (hiddenFieldByteArray.isEmpty()) {
            HiddenState.Empty(value)
        } else {
            if (isConcealed) {
                HiddenState.Concealed(value)
            } else {
                HiddenState.Revealed(value, hiddenFieldByteArray.decodeToString())
            }
        }
        CustomFieldContent.Hidden(label = this.label, value = hiddenState)
    }

    is CustomField.Text -> {
        CustomFieldContent.Text(label = this.label, value = this.value)
    }

    is CustomField.Totp -> {
        val totpFieldByteArray = encryptionContext.decrypt(value.toEncryptedByteArray())
        val hiddenState = if (totpFieldByteArray.isEmpty()) {
            HiddenState.Empty(value)
        } else {
            if (isConcealed) {
                HiddenState.Concealed(value)
            } else {
                HiddenState.Revealed(value, totpFieldByteArray.decodeToString())
            }
        }
        CustomFieldContent.Totp(label = this.label, value = hiddenState)
    }
}

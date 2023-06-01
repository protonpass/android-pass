package proton.android.pass.featureitemcreate.impl.login.customfields

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.featureitemcreate.impl.login.LoginItemValidationErrors

class ThemeTotpCustomFieldInput :
    ThemePairPreviewProvider<TotpCustomFieldInput>(TotpCustomFieldPreviewProvider())

class TotpCustomFieldPreviewProvider : PreviewParameterProvider<TotpCustomFieldInput> {
    override val values: Sequence<TotpCustomFieldInput>
        get() = sequence {
            for (text in listOf("", "mytotp")) {
                for (isEnabled in listOf(true, false)) {
                    yield(TotpCustomFieldInput(text, isEnabled, null))
                }
                yield(
                    TotpCustomFieldInput(
                        text,
                        false,
                        LoginItemValidationErrors.CustomFieldValidationError.EmptyField(1)
                    )
                )
                yield(
                    TotpCustomFieldInput(
                        text,
                        false,
                        LoginItemValidationErrors.CustomFieldValidationError.InvalidTotp(1)
                    )
                )
            }
        }
}

data class TotpCustomFieldInput(
    val text: String,
    val isEnabled: Boolean,
    val error: LoginItemValidationErrors.CustomFieldValidationError?
)

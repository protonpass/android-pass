package proton.android.pass.featureitemcreate.impl.login.customfields

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.commonui.api.ThemePairPreviewProvider

class ThemeCustomFieldPreviewProvider : ThemePairPreviewProvider<CustomFieldInput>(CustomFieldPreviewProvider())

class CustomFieldPreviewProvider : PreviewParameterProvider<CustomFieldInput> {
    override val values: Sequence<CustomFieldInput>
        get() = sequence {
            for (text in listOf("", "value")) {
                for (enabled in listOf(true, false)) {
                    yield(CustomFieldInput(text, enabled))
                }
            }
        }
}

data class CustomFieldInput(
    val text: String,
    val enabled: Boolean
)

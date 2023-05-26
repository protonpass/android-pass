package proton.android.pass.featureitemcreate.impl.login.customfields

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.featureitemcreate.impl.login.TotpInput
import proton.pass.domain.CustomFieldContent

@Composable
fun TotpCustomFieldEntry(
    modifier: Modifier = Modifier,
    content: CustomFieldContent.Totp,
    canEdit: Boolean,
    onChange: (String) -> Unit
) {
    TotpInput(
        modifier = modifier,
        value = content.value,
        enabled = canEdit,
        isError = false,
        onTotpChanged = onChange,
        onFocus = {}
    )
}

@Preview
@Composable
fun TotpCustomFieldEntryPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    PassTheme(isDark = input.first) {
        Surface {
            TotpCustomFieldEntry(
                content = CustomFieldContent.Totp(
                    label = "label",
                    value = "value"
                ),
                canEdit = input.second,
                onChange = {}
            )
        }
    }
}

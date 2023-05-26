package proton.android.pass.featureitemcreate.impl.login.customfields

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.featureitemcreate.impl.R
import proton.android.pass.featureitemcreate.impl.login.PasswordInput
import proton.pass.domain.CustomFieldContent
import proton.pass.domain.HiddenState
import me.proton.core.presentation.R as CoreR

@Composable
fun HiddenCustomFieldEntry(
    modifier: Modifier = Modifier,
    content: CustomFieldContent.Hidden,
    canEdit: Boolean,
    onChange: (String) -> Unit
) {
    val value = when (val state = content.value) {
        is HiddenState.Concealed -> ""
        is HiddenState.Revealed -> state.clearText
    }
    PasswordInput(
        modifier = modifier,
        label = content.label,
        value = value,
        icon = CoreR.drawable.ic_proton_lock,
        placeholder = stringResource(R.string.custom_field_hidden_placeholder),
        iconContentDescription = stringResource(R.string.custom_field_hidden_icon_content_description),
        isEditAllowed = canEdit,
        onChange = onChange,
        onFocus = {}
    )
}

@Preview
@Composable
fun HiddenCustomFieldEntryPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    PassTheme(isDark = input.first) {
        Surface {
            HiddenCustomFieldEntry(
                content = CustomFieldContent.Hidden(
                    label = "label",
                    value = HiddenState.Revealed("", "value")
                ),
                canEdit = input.second,
                onChange = {}
            )
        }
    }
}

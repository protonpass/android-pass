package proton.android.pass.featureitemcreate.impl.login.customfields

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.composecomponents.impl.form.SimpleNoteSection
import proton.android.pass.featureitemcreate.impl.R
import proton.pass.domain.CustomFieldContent
import me.proton.core.presentation.R as CoreR

@Composable
fun TextCustomFieldEntry(
    modifier: Modifier = Modifier,
    content: CustomFieldContent.Text,
    canEdit: Boolean,
    onChange: (String) -> Unit
) {
    SimpleNoteSection(
        modifier = modifier,
        value = content.value,
        label = content.label,
        placeholder = stringResource(R.string.custom_field_text_placeholder),
        icon = CoreR.drawable.ic_proton_text_align_left,
        iconContentDescription = stringResource(R.string.custom_field_text_icon_content_description),
        enabled = canEdit,
        onChange = onChange
    )
}

@Preview
@Composable
fun TextCustomFieldEntryPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    PassTheme(isDark = input.first) {
        Surface {
            TextCustomFieldEntry(
                content = CustomFieldContent.Text(label = "label", value = "value"),
                canEdit = input.second,
                onChange = {}
            )
        }
    }
}

package proton.android.pass.featureitemcreate.impl.login.customfields

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldLabel
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.composecomponents.impl.form.SmallCrossIconButton
import proton.android.pass.featureitemcreate.impl.R
import proton.android.pass.featureitemcreate.impl.login.LoginCustomField
import proton.pass.domain.CustomFieldContent
import me.proton.core.presentation.R as CoreR

@Composable
fun TextCustomFieldEntry(
    modifier: Modifier = Modifier,
    content: CustomFieldContent.Text,
    index: Int,
    canEdit: Boolean,
    onChange: (String) -> Unit,
    onFocusChange: (LoginCustomField, Boolean) -> Unit,
    onOptionsClick: () -> Unit
) {
    ProtonTextField(
        modifier = modifier
            .roundedContainerNorm()
            .padding(start = 0.dp, top = 16.dp, end = 4.dp, bottom = 16.dp),
        textStyle = ProtonTheme.typography.defaultNorm(canEdit),
        label = { ProtonTextFieldLabel(text = content.label) },
        placeholder = { ProtonTextFieldPlaceHolder(text = stringResource(R.string.custom_field_text_placeholder)) },
        editable = canEdit,
        value = content.value,
        onChange = onChange,
        singleLine = false,
        moveToNextOnEnter = true,
        leadingIcon = {
            Icon(
                painter = painterResource(CoreR.drawable.ic_proton_text_align_left),
                contentDescription = stringResource(R.string.custom_field_text_icon_content_description),
                tint = PassTheme.colors.textWeak
            )
        },
        trailingIcon = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (content.value.isNotEmpty()) {
                    SmallCrossIconButton { onChange("") }
                }
                CustomFieldOptionsButton(onClick = onOptionsClick)
            }
        },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        onFocusChange = { onFocusChange(LoginCustomField.CustomFieldText(index), it) }
    )
}

@Preview
@Composable
fun TextCustomFieldEntryPreview(
    @PreviewParameter(ThemeCustomFieldPreviewProvider::class) input: Pair<Boolean, CustomFieldInput>
) {
    PassTheme(isDark = input.first) {
        Surface {
            TextCustomFieldEntry(
                content = CustomFieldContent.Text(label = "label", value = input.second.text),
                canEdit = input.second.enabled,
                index = 0,
                onChange = {},
                onFocusChange = { _, _ -> },
                onOptionsClick = {}
            )
        }
    }
}

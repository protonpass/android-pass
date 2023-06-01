package proton.android.pass.featureitemcreate.impl.login.customfields

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
import proton.pass.domain.CustomFieldContent
import proton.pass.domain.HiddenState
import me.proton.core.presentation.R as CoreR

@Composable
fun HiddenCustomFieldEntry(
    modifier: Modifier = Modifier,
    content: CustomFieldContent.Hidden,
    canEdit: Boolean,
    onChange: (String) -> Unit,
    onOptionsClick: () -> Unit
) {
    val value = when (val state = content.value) {
        is HiddenState.Concealed -> ""
        is HiddenState.Revealed -> state.clearText
    }
    var isFocused by remember { mutableStateOf(false) }
    val visualTransformation = if (isFocused) {
        VisualTransformation.None
    } else {
        PasswordVisualTransformation()
    }
    Box(modifier = modifier.roundedContainerNorm()) {
        ProtonTextField(
            modifier = modifier.padding(start = 0.dp, top = 16.dp, end = 4.dp, bottom = 16.dp),
            value = value,
            editable = canEdit,
            moveToNextOnEnter = true,
            singleLine = false,
            textStyle = ProtonTheme.typography.defaultNorm(canEdit),
            onChange = onChange,
            label = { ProtonTextFieldLabel(text = content.label) },
            placeholder = {
                ProtonTextFieldPlaceHolder(text = stringResource(R.string.custom_field_hidden_placeholder))
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(CoreR.drawable.ic_proton_eye_slash),
                    contentDescription = stringResource(R.string.custom_field_hidden_icon_content_description),
                    tint = ProtonTheme.colors.iconWeak
                )
            },
            trailingIcon = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (value.isNotEmpty()) {
                        SmallCrossIconButton { onChange("") }
                    }
                    CustomFieldOptionsButton(onClick = onOptionsClick)
                }
            },
            visualTransformation = visualTransformation,
            onFocusChange = {
                isFocused = it
            }
        )
    }
}

@Preview
@Composable
fun HiddenCustomFieldEntryPreview(
    @PreviewParameter(ThemeCustomFieldPreviewProvider::class) input: Pair<Boolean, CustomFieldInput>
) {
    PassTheme(isDark = input.first) {
        Surface {
            HiddenCustomFieldEntry(
                content = CustomFieldContent.Hidden(
                    label = "label",
                    value = HiddenState.Revealed("", input.second.text)
                ),
                canEdit = input.second.enabled,
                onChange = {},
                onOptionsClick = {}
            )
        }
    }
}

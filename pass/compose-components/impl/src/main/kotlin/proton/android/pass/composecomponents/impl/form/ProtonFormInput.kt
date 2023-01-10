package proton.android.pass.composecomponents.impl.form

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import proton.android.pass.composecomponents.impl.R
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.caption
import proton.android.pass.commonui.api.ThemePairPreviewProvider

@Composable
fun ProtonFormInput(
    title: String,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    placeholder: String? = null,
    required: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    moveToNextOnEnter: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    editable: Boolean = true,
    isError: Boolean = false,
    errorMessage: String = ""
) {
    Column(modifier = modifier) {
        ProtonTextTitle(title)
        ProtonTextField(
            value = value,
            onChange = onChange,
            placeholder = placeholder,
            trailingIcon = trailingIcon,
            singleLine = singleLine,
            visualTransformation = visualTransformation,
            moveToNextOnEnter = moveToNextOnEnter,
            modifier = contentModifier
                .padding(top = 8.dp)
                .fillMaxWidth(1.0f),
            editable = editable,
            isError = isError
        )
        if (isError) {
            Text(
                text = errorMessage,
                modifier = Modifier.padding(top = 4.dp),
                fontSize = 12.sp,
                style = ProtonTheme.typography.caption,
                color = ProtonTheme.colors.notificationError
            )
        } else if (required) {
            Text(
                text = stringResource(R.string.field_required_indicator),
                modifier = Modifier.padding(top = 4.dp),
                fontSize = 12.sp,
                style = ProtonTheme.typography.caption,
                color = ProtonTheme.colors.textWeak
            )
        }
    }
}

class ThemeAndProtonFormInputProvider :
    ThemePairPreviewProvider<ProtonFormInputPreviewData>(ProtonFormInputPreviewProvider())

@Preview
@Composable
fun ProtonFormInputPreview(
    @PreviewParameter(ThemeAndProtonFormInputProvider::class) input: Pair<Boolean, ProtonFormInputPreviewData>
) {
    ProtonTheme(isDark = input.first) {
        Surface {
            ProtonFormInput(
                title = "Title",
                placeholder = "Name",
                value = input.second.value,
                required = input.second.isRequired,
                editable = input.second.isEditable,
                isError = input.second.isError,
                errorMessage = input.second.errorMessage,
                onChange = {}
            )
        }
    }
}

package me.proton.core.pass.presentation.components.form

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.caption
import me.proton.core.pass.presentation.R
import me.proton.core.pass.presentation.components.previewproviders.ProtonFormInputPreviewData
import me.proton.core.pass.presentation.components.previewproviders.ProtonFormInputPreviewProvider

@Composable
fun ProtonFormInput(
    @StringRes title: Int,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    @StringRes placeholder: Int? = null,
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
            modifier = Modifier.padding(top = 8.dp).fillMaxWidth(1.0f),
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

@Preview(showBackground = true)
@Composable
internal fun Preview_ProtonFormInput(
    @PreviewParameter(ProtonFormInputPreviewProvider::class) data: ProtonFormInputPreviewData
) {
    ProtonTheme {
        ProtonFormInput(
            title = R.string.field_title_title,
            placeholder = R.string.field_title_hint,
            value = data.value,
            required = data.isRequired,
            editable = data.isEditable,
            isError = data.isError,
            errorMessage = data.errorMessage,
            onChange = {}
        )
    }
}

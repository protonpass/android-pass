package me.proton.pass.presentation.components.form

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.previewproviders.TitleInputPreviewData
import me.proton.pass.presentation.components.previewproviders.TitleInputPreviewProvider

@Composable
fun TitleInput(
    value: String,
    onChange: (String) -> Unit,
    onTitleRequiredError: Boolean
) {
    ProtonFormInput(
        title = R.string.field_title_title,
        placeholder = R.string.field_title_hint,
        value = value,
        onChange = onChange,
        required = true,
        modifier = Modifier.padding(top = 8.dp),
        isError = onTitleRequiredError,
        errorMessage = stringResource(id = R.string.field_title_is_blank)
    )
}

@Preview
@Composable
fun TitleInputPreview(
    @PreviewParameter(TitleInputPreviewProvider::class) data: TitleInputPreviewData
) {
    ProtonTheme {
        Surface {
            TitleInput(
                value = "test",
                onChange = {},
                onTitleRequiredError = false
            )
        }
    }
}

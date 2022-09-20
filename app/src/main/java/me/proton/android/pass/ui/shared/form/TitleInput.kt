package me.proton.android.pass.ui.shared.form

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.proton.android.pass.R
import me.proton.android.pass.ui.shared.ProtonFormInput

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
        isError = onTitleRequiredError
    )
}

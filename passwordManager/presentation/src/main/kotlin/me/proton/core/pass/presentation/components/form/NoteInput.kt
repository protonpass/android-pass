package me.proton.core.pass.presentation.components.form

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.pass.presentation.R
import me.proton.core.pass.presentation.components.previewproviders.NoteInputPreviewProvider

@Composable
fun NoteInput(value: String, onChange: (String) -> Unit) {
    ProtonFormInput(
        title = R.string.field_note_title,
        placeholder = R.string.field_note_hint,
        value = value,
        onChange = onChange,
        modifier = Modifier.padding(top = 28.dp),
        singleLine = false,
        moveToNextOnEnter = false
    )
}

@Preview(showBackground = true)
@Composable
fun Preview_NoteInput(@PreviewParameter(NoteInputPreviewProvider::class) value: String) {
    ProtonTheme {
        NoteInput(value, {})
    }
}

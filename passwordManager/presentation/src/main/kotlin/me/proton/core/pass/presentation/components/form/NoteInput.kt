package me.proton.android.pass.ui.shared.form

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.proton.android.pass.ui.shared.ProtonFormInput
import me.proton.core.pass.presentation.R

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

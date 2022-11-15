package me.proton.pass.presentation.components.form

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.commonui.api.ThemePairPreviewProvider
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.previewproviders.NoteInputPreviewParameter
import me.proton.pass.presentation.components.previewproviders.NoteInputPreviewProvider
import me.proton.pass.presentation.uievents.value

@Composable
fun NoteInput(
    value: String,
    enabled: Boolean = true,
    onChange: (String) -> Unit
) {
    ProtonFormInput(
        title = R.string.field_note_title,
        placeholder = R.string.field_note_hint,
        editable = enabled,
        value = value,
        onChange = onChange,
        modifier = Modifier.padding(top = 28.dp),
        singleLine = false,
        moveToNextOnEnter = false
    )
}


class ThemedNoteInputPreviewProvider :
    ThemePairPreviewProvider<NoteInputPreviewParameter>(NoteInputPreviewProvider())

@Preview
@Composable
fun NoteInputPreview(
    @PreviewParameter(ThemedNoteInputPreviewProvider::class) input: Pair<Boolean, NoteInputPreviewParameter>
) {
    ProtonTheme(isDark = input.first) {
        Surface {
            NoteInput(
                value = input.second.value,
                enabled = input.second.enabled,
                onChange = {}
            )
        }
    }
}

package proton.android.pass.composecomponents.impl.form

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.R

@Composable
fun NoteInput(
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    value: String,
    enabled: Boolean = true,
    onChange: (String) -> Unit
) {
    ProtonFormInput(
        modifier = modifier.padding(top = 28.dp),
        contentModifier = contentModifier,
        title = stringResource(id = R.string.field_note_title),
        placeholder = stringResource(id = R.string.field_note_hint),
        editable = enabled,
        value = value,
        onChange = onChange,
        singleLine = false,
        moveToNextOnEnter = false,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
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

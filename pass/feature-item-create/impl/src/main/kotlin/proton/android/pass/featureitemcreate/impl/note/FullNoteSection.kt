package proton.android.pass.featureitemcreate.impl.note

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.default
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.form.NoteInputPreviewParameter
import proton.android.pass.composecomponents.impl.form.NoteInputPreviewProvider
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.composecomponents.impl.form.SimpleNoteSection
import proton.android.pass.featureitemcreate.impl.R

@Composable
fun FullNoteSection(
    modifier: Modifier = Modifier,
    value: String,
    enabled: Boolean = true,
    onChange: (String) -> Unit
) {
    ProtonTextField(
        modifier = modifier,
        textStyle = ProtonTheme.typography.default(enabled),
        placeholder = { ProtonTextFieldPlaceHolder(text = stringResource(id = R.string.note_hint)) },
        editable = enabled,
        value = value,
        onChange = onChange,
        singleLine = false,
        moveToNextOnEnter = false,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
    )
}

class ThemedFullNoteInputPreviewProvider :
    ThemePairPreviewProvider<NoteInputPreviewParameter>(NoteInputPreviewProvider())

@Preview
@Composable
fun FullNoteSectionPreview(
    @PreviewParameter(ThemedFullNoteInputPreviewProvider::class) input: Pair<Boolean, NoteInputPreviewParameter>
) {
    PassTheme(isDark = input.first) {
        Surface {
            SimpleNoteSection(
                value = input.second.value,
                enabled = input.second.enabled,
                onChange = {}
            )
        }
    }
}

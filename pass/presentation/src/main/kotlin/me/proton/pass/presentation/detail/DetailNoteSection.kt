package me.proton.pass.presentation.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.commonui.api.ThemePairPreviewProvider
import me.proton.pass.presentation.R

@Composable
fun DetailNoteSection(
    modifier: Modifier = Modifier,
    text: String
) {
    if (text.isBlank()) return

    Column(modifier = modifier.fillMaxWidth()) {
        DetailSectionTitle(text = stringResource(R.string.field_detail_note_title))
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = text,
            fontWeight = FontWeight.W400,
            fontSize = 16.sp,
            color = ProtonTheme.colors.textWeak
        )
    }
}

class ThemedDetailNoteSectionPreviewProvider :
    ThemePairPreviewProvider<String>(NoteDetailSectionPreviewProvider())

@Preview
@Composable
fun DetailNoteSectionPreview(
    @PreviewParameter(ThemedDetailNoteSectionPreviewProvider::class) input: Pair<Boolean, String>
) {
    ProtonTheme(isDark = input.first) {
        Surface {
            DetailNoteSection(text = input.second)
        }
    }
}

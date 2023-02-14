package proton.android.pass.featureitemdetail.impl.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassColors
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.container.RoundedCornersColumn
import proton.android.pass.featureitemdetail.impl.NoteDetailSectionPreviewProvider
import proton.android.pass.featureitemdetail.impl.R
import proton.android.pass.featureitemdetail.impl.SectionSubtitle
import proton.android.pass.featureitemdetail.impl.SectionTitle

@Composable
fun NoteSection(
    modifier: Modifier = Modifier,
    text: String,
    accentColor: Color
) {
    if (text.isBlank()) return
    RoundedCornersColumn(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_note),
                contentDescription = stringResource(R.string.note_section_icon_content_description),
                tint = accentColor
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                SectionTitle(text = stringResource(R.string.field_detail_note_title))
                Spacer(modifier = Modifier.height(8.dp))
                SectionSubtitle(text = text)
            }
        }
    }
}

class ThemedDetailNoteSectionPreviewProvider :
    ThemePairPreviewProvider<String>(NoteDetailSectionPreviewProvider())

@Preview
@Composable
fun NoteSectionPreview(
    @PreviewParameter(ThemedDetailNoteSectionPreviewProvider::class) input: Pair<Boolean, String>
) {
    ProtonTheme(isDark = input.first) {
        Surface {
            NoteSection(text = input.second, accentColor = PassColors.PurpleAccent)
        }
    }
}

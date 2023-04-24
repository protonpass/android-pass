package proton.android.pass.featureitemdetail.impl.note

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.featureitemdetail.impl.common.ItemTitleInput
import proton.android.pass.featureitemdetail.impl.common.ItemTitleText
import proton.android.pass.featureitemdetail.impl.common.MoreInfo
import proton.android.pass.featureitemdetail.impl.common.MoreInfoUiState
import proton.android.pass.featureitemdetail.impl.common.ThemeItemTitleProvider
import proton.android.pass.featureitemdetail.impl.common.VaultNameSubtitle
import proton.pass.domain.Vault

@Composable
fun NoteContent(
    modifier: Modifier = Modifier,
    name: String,
    note: String,
    vault: Vault?,
    moreInfoUiState: MoreInfoUiState
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Column {
            ItemTitleText(text = name, maxLines = Int.MAX_VALUE)
            VaultNameSubtitle(vault = vault)
        }
        SelectionContainer(modifier = Modifier.fillMaxWidth()) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = note,
                style = ProtonTheme.typography.defaultNorm
            )
        }
        MoreInfo(moreInfoUiState = moreInfoUiState)
    }
}

@Preview
@Composable
fun NoteContentPreview(
    @PreviewParameter(ThemeItemTitleProvider::class) input: Pair<Boolean, ItemTitleInput>
) {
    PassTheme(isDark = input.first) {
        Surface {
            NoteContent(
                name = input.second.title,
                note = "Note body",
                vault = input.second.vault,
                // We don't care about the MoreInfo as we are not showing it
                moreInfoUiState = MoreInfoUiState.Initial
            )
        }
    }
}

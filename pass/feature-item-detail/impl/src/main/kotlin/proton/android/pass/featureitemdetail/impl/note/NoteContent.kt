package proton.android.pass.featureitemdetail.impl.note

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.default
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.featureitemdetail.impl.common.ItemTitleText
import proton.android.pass.featureitemdetail.impl.common.MoreInfo
import proton.android.pass.featureitemdetail.impl.common.MoreInfoUiState

@Composable
fun NoteContent(
    modifier: Modifier = Modifier,
    model: NoteDetailUiState,
    moreInfoUiState: MoreInfoUiState,
    onCopyToClipboard: () -> Unit
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        ItemTitleText(text = model.title, maxLines = Int.MAX_VALUE)
        Text(
            modifier = Modifier
                .clickable { onCopyToClipboard() }
                .fillMaxWidth(),
            text = model.note,
            style = ProtonTheme.typography.default
        )
        MoreInfo(moreInfoUiState = moreInfoUiState)
    }
}

@Preview
@Composable
fun NoteContentPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    ProtonTheme(isDark = isDark) {
        Surface {
            NoteContent(
                model = NoteDetailUiState(
                    title = "Note title",
                    note = "Note body",
                    isLoading = false,
                    isItemSentToTrash = false
                ),
                onCopyToClipboard = {},

                // We don't care about the MoreInfo as we are not showing it
                moreInfoUiState = MoreInfoUiState.Initial
            )
        }
    }
}

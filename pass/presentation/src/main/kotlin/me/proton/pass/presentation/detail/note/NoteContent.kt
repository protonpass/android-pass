package me.proton.pass.presentation.detail.note

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.default

@Composable
fun NoteContent(
    modifier: Modifier = Modifier,
    model: NoteDetailUiState,
    onCopyToClipboard: () -> Unit
) {
    Text(
        modifier = modifier
            .clickable { onCopyToClipboard() }
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 32.dp),
        text = model.note,
        style = ProtonTheme.typography.default
    )
}

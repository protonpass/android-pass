package me.proton.pass.presentation.detail.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.proton.pass.presentation.detail.DetailNoteSection

@Composable
fun LoginContent(
    modifier: Modifier = Modifier,
    model: LoginUiModel,
    onTogglePasswordClick: () -> Unit,
    onCopyPasswordClick: () -> Unit,
    storeToClipboard: (contents: String?, fieldName: String) -> Unit
) {
    Column(modifier.padding(horizontal = 16.dp)) {
        LoginUsernameRow(
            username = model.username,
            storeToClipboard = storeToClipboard
        )
        Spacer(modifier = Modifier.height(12.dp))
        LoginPasswordRow(
            password = model.password,
            onTogglePasswordClick = onTogglePasswordClick,
            onCopyPasswordClick = onCopyPasswordClick
        )
        if (model.websites.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            WebsiteSection(
                modifier = Modifier.padding(start = 16.dp),
                websites = model.websites
            )
        }
        Spacer(modifier = Modifier.height(28.dp))
        DetailNoteSection(
            modifier = Modifier.padding(start = 16.dp),
            text = model.note
        )
    }
}

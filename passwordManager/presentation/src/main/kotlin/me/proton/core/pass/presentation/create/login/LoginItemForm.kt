package me.proton.core.pass.presentation.create.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.proton.core.pass.presentation.components.form.NoteInput
import me.proton.core.pass.presentation.components.form.TitleInput

@Composable
internal fun LoginItemForm(
    modifier: Modifier = Modifier,
    loginItem: LoginItem,
    onTitleChange: (String) -> Unit,
    onTitleRequiredError: Boolean,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onWebsiteChange: OnWebsiteChange,
    onNoteChange: (String) -> Unit,
    onGeneratePasswordClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        TitleInput(
            value = loginItem.title,
            onChange = onTitleChange,
            onTitleRequiredError = onTitleRequiredError
        )
        UsernameInput(
            value = loginItem.username,
            onChange = onUsernameChange,
            onGenerateAliasClick = {}
        )
        PasswordInput(
            value = loginItem.password,
            onChange = onPasswordChange,
            onGeneratePasswordClick = onGeneratePasswordClick
        )
        Spacer(modifier = Modifier.height(20.dp))
        WebsitesSection(websites = loginItem.websiteAddresses, onWebsitesChange = onWebsiteChange)
        NoteInput(value = loginItem.note, onChange = onNoteChange)
    }
}

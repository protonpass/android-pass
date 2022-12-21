package me.proton.pass.presentation.create.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toImmutableList
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.common.PassOutlinedButton
import me.proton.pass.presentation.components.form.NoteInput
import me.proton.pass.presentation.components.form.TitleInput

@Composable
internal fun LoginItemForm(
    modifier: Modifier = Modifier,
    isEditAllowed: Boolean,
    loginItem: LoginItem,
    showCreateAliasButton: Boolean,
    canDelete: Boolean,
    onTitleChange: (String) -> Unit,
    onTitleRequiredError: Boolean,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onWebsiteChange: OnWebsiteChange,
    doesWebsiteIndexHaveError: (Int) -> Boolean,
    focusLastWebsite: Boolean,
    onNoteChange: (String) -> Unit,
    onGeneratePasswordClick: () -> Unit,
    onCreateAliasClick: () -> Unit,
    canUpdateUsername: Boolean,
    onAliasOptionsClick: () -> Unit,
    onDeleteClick: () -> Unit
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
            onTitleRequiredError = onTitleRequiredError,
            enabled = isEditAllowed
        )
        UsernameInput(
            value = loginItem.username,
            showCreateAliasButton = showCreateAliasButton,
            canUpdateUsername = canUpdateUsername,
            isEditAllowed = isEditAllowed,
            onChange = onUsernameChange,
            onGenerateAliasClick = onCreateAliasClick,
            onAliasOptionsClick = onAliasOptionsClick
        )
        PasswordInput(
            value = loginItem.password,
            isEditAllowed = isEditAllowed,
            onChange = onPasswordChange,
            onGeneratePasswordClick = onGeneratePasswordClick
        )
        Spacer(modifier = Modifier.height(20.dp))
        WebsitesSection(
            websites = loginItem.websiteAddresses.toImmutableList(),
            isEditAllowed = isEditAllowed,
            onWebsitesChange = onWebsiteChange,
            focusLastWebsite = focusLastWebsite,
            doesWebsiteIndexHaveError = doesWebsiteIndexHaveError
        )
        NoteInput(
            contentModifier = Modifier.height(100.dp),
            value = loginItem.note,
            enabled = isEditAllowed,
            onChange = onNoteChange
        )

        if (canDelete) {
            Spacer(Modifier.height(height = 24.dp))
            PassOutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.action_move_to_trash),
                color = ProtonTheme.colors.notificationError,
                onClick = onDeleteClick
            )
        }
    }
}

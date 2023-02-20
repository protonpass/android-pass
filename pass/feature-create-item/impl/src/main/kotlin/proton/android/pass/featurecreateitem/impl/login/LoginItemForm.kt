package proton.android.pass.featurecreateitem.impl.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toImmutableList
import proton.android.pass.commonuimodels.api.ShareUiModel
import proton.android.pass.composecomponents.impl.form.NoteSection
import proton.android.pass.composecomponents.impl.form.TitleSection

@Suppress("UnusedPrivateMember")
@Composable
internal fun LoginItemForm(
    modifier: Modifier = Modifier,
    isEditAllowed: Boolean,
    loginItem: LoginItem,
    selectedShare: ShareUiModel?,
    showCreateAliasButton: Boolean,
    isUpdate: Boolean,
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
    onVaultSelectorClick: () -> Unit,
    onAddTotpClick: () -> Unit,
    onDeleteTotpClick: () -> Unit,
    onLinkedAppDelete: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TitleSection(
            value = loginItem.title,
            onChange = onTitleChange,
            onTitleRequiredError = onTitleRequiredError,
            enabled = isEditAllowed
        )
        MainLoginSection(
            loginItem = loginItem,
            showCreateAliasButton = showCreateAliasButton,
            canUpdateUsername = canUpdateUsername,
            isEditAllowed = isEditAllowed,
            onUsernameChange = onUsernameChange,
            onCreateAliasClick = onCreateAliasClick,
            onAliasOptionsClick = onAliasOptionsClick,
            onPasswordChange = onPasswordChange,
            onGeneratePasswordClick = onGeneratePasswordClick,
            onAddTotpClick = onAddTotpClick,
            onDeleteTotpClick = onDeleteTotpClick
        )
        WebsitesSection(
            websites = loginItem.websiteAddresses.toImmutableList(),
            isEditAllowed = isEditAllowed,
            onWebsitesChange = onWebsiteChange,
            focusLastWebsite = focusLastWebsite,
            doesWebsiteIndexHaveError = doesWebsiteIndexHaveError
        )
        NoteSection(
            value = loginItem.note,
            enabled = isEditAllowed,

            onChange = onNoteChange
        )
        if (isUpdate) {
/*            LinkedAppsListSection(
                list = loginItem.packageNames.toImmutableSet(),
                isEditable = true,
                onLinkedAppDelete = onLinkedAppDelete
            )*/
        }
        if (!isUpdate) {
            selectedShare?.name?.let {
                VaultSelector(
                    contentText = it,
                    isEditAllowed = true,
                    onClick = onVaultSelectorClick
                )
            }
        }
    }
}


package proton.android.pass.featurecreateitem.impl.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toImmutableList
import proton.android.pass.commonuimodels.api.ShareUiModel
import proton.android.pass.composecomponents.impl.form.NoteSection
import proton.android.pass.composecomponents.impl.form.TitleSection
import proton.android.pass.featurecreateitem.impl.login.LoginStickyFormOptionsContentType.AddTotp
import proton.android.pass.featurecreateitem.impl.login.LoginStickyFormOptionsContentType.AliasOptions
import proton.android.pass.featurecreateitem.impl.login.LoginStickyFormOptionsContentType.GeneratePassword
import proton.android.pass.featurecreateitem.impl.login.LoginStickyFormOptionsContentType.None

@OptIn(ExperimentalComposeUiApi::class)
@Suppress("UnusedPrivateMember")
@Composable
internal fun LoginItemForm(
    modifier: Modifier = Modifier,
    isEditAllowed: Boolean,
    loginItem: LoginItem,
    selectedShare: ShareUiModel?,
    showCreateAliasButton: Boolean,
    primaryEmail: String?,
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
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(modifier = modifier) {
        var currentStickyFormOption by remember { mutableStateOf(None) }

        Column(
            modifier = Modifier
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
                canUpdateUsername = canUpdateUsername,
                isEditAllowed = isEditAllowed,
                onUsernameChange = onUsernameChange,
                onUsernameFocus = { isFocused ->
                    currentStickyFormOption = if (isFocused) {
                        AliasOptions
                    } else {
                        None
                    }
                },
                onAliasOptionsClick = onAliasOptionsClick,
                onPasswordChange = onPasswordChange,
                onPasswordFocus = { isFocused ->
                    currentStickyFormOption = if (isFocused) {
                        GeneratePassword
                    } else {
                        None
                    }
                },
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
        val isCurrentStickyVisible by remember(currentStickyFormOption) {
            mutableStateOf(currentStickyFormOption != None)
        }
        AnimatedVisibility(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .imePadding(),
            visible = isCurrentStickyVisible,
            enter = expandVertically()
        ) {
            when (currentStickyFormOption) {
                GeneratePassword ->
                    StickyGeneratePassword(
                        onClick = {
                            onGeneratePasswordClick()
                            keyboardController?.hide()
                        }
                    )
                AliasOptions -> StickyUsernameOptions(
                    primaryEmail = primaryEmail,
                    onCreateAliasClick = {
                        onCreateAliasClick()
                        keyboardController?.hide()
                    },
                    onPrefillCurrentEmailClick = {
                        onUsernameChange(it)
                        keyboardController?.hide()
                    }
                )
                AddTotp -> TODO()
                None -> {}
            }
        }
    }
}

package proton.android.pass.featureitemcreate.impl.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.composecomponents.impl.form.SimpleNoteSection
import proton.android.pass.composecomponents.impl.form.TitleVaultSelectionSection
import proton.android.pass.composecomponents.impl.item.LinkedAppsListSection
import proton.android.pass.featureitemcreate.impl.login.LoginStickyFormOptionsContentType.AddTotp
import proton.android.pass.featureitemcreate.impl.login.LoginStickyFormOptionsContentType.AliasOptions
import proton.android.pass.featureitemcreate.impl.login.LoginStickyFormOptionsContentType.GeneratePassword
import proton.android.pass.featureitemcreate.impl.login.LoginStickyFormOptionsContentType.None
import proton.pass.domain.VaultWithItemCount

@OptIn(ExperimentalComposeUiApi::class)
@Suppress("UnusedPrivateMember")
@Composable
internal fun LoginItemForm(
    modifier: Modifier = Modifier,
    isEditAllowed: Boolean,
    loginItem: LoginItem,
    selectedShare: VaultWithItemCount?,
    showCreateAliasButton: Boolean,
    primaryEmail: String?,
    isUpdate: Boolean,
    showVaultSelector: Boolean,
    onTitleRequiredError: Boolean,
    isTotpError: Boolean,
    focusLastWebsite: Boolean,
    canUpdateUsername: Boolean,
    onTitleChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTotpChange: (String) -> Unit,
    onWebsiteChange: OnWebsiteChange,
    doesWebsiteIndexHaveError: (Int) -> Boolean,
    onNoteChange: (String) -> Unit,
    onGeneratePasswordClick: () -> Unit,
    onCreateAliasClick: () -> Unit,
    onAliasOptionsClick: () -> Unit,
    onVaultSelectorClick: () -> Unit,
    onPasteTotpClick: () -> Unit,
    onScanTotpClick: () -> Unit,
    onLinkedAppDelete: (PackageInfoUi) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(modifier = modifier) {
        var currentStickyFormOption by remember { mutableStateOf(None) }
        val isCurrentStickyVisible by remember(currentStickyFormOption) {
            mutableStateOf(currentStickyFormOption != None)
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TitleVaultSelectionSection(
                titleValue = loginItem.title,
                onTitleChanged = onTitleChange,
                onTitleRequiredError = onTitleRequiredError,
                enabled = isEditAllowed,
                showVaultSelector = showVaultSelector,
                vaultName = selectedShare?.vault?.name,
                vaultColor = selectedShare?.vault?.color,
                vaultIcon = selectedShare?.vault?.icon,
                onVaultClicked = onVaultSelectorClick
            )
            MainLoginSection(
                loginItem = loginItem,
                canUpdateUsername = canUpdateUsername,
                isEditAllowed = isEditAllowed,
                isTotpError = isTotpError,
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
                onTotpChanged = onTotpChange,
                onTotpFocus = { isFocused ->
                    currentStickyFormOption = if (isFocused) {
                        AddTotp
                    } else {
                        None
                    }
                }
            )
            WebsitesSection(
                websites = loginItem.websiteAddresses.toImmutableList(),
                isEditAllowed = isEditAllowed,
                onWebsitesChange = onWebsiteChange,
                focusLastWebsite = focusLastWebsite,
                doesWebsiteIndexHaveError = doesWebsiteIndexHaveError
            )
            SimpleNoteSection(
                value = loginItem.note,
                enabled = isEditAllowed,
                onChange = onNoteChange
            )
            if (isUpdate) {
                LinkedAppsListSection(
                    packageInfoUiSet = loginItem.packageInfoSet,
                    isEditable = true,
                    onLinkedAppDelete = onLinkedAppDelete
                )
            }
            if (isCurrentStickyVisible) {
                Spacer(modifier = Modifier.height(48.dp))
            }
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
                    showCreateAliasButton = showCreateAliasButton,
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
                AddTotp -> StickyTotpOptions(
                    onPasteCode = {
                        onPasteTotpClick()
                        keyboardController?.hide()
                    },
                    onScanCode = {
                        onScanTotpClick()
                        keyboardController?.hide()
                    }
                )
                None -> {}
            }
        }
    }
}

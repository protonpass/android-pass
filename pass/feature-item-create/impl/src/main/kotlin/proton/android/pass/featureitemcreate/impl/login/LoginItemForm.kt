package proton.android.pass.featureitemcreate.impl.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.composecomponents.impl.form.SimpleNoteSection
import proton.android.pass.composecomponents.impl.item.LinkedAppsListSection
import proton.android.pass.featureitemcreate.impl.login.LoginStickyFormOptionsContentType.AddTotp
import proton.android.pass.featureitemcreate.impl.login.LoginStickyFormOptionsContentType.AliasOptions
import proton.android.pass.featureitemcreate.impl.login.LoginStickyFormOptionsContentType.GeneratePassword
import proton.android.pass.featureitemcreate.impl.login.LoginStickyFormOptionsContentType.None
import proton.android.pass.featureitemcreate.impl.login.customfields.CustomFieldsContent
import proton.pass.domain.ItemContents

@OptIn(ExperimentalComposeUiApi::class)
@Suppress("UnusedPrivateMember")
@Composable
internal fun LoginItemForm(
    modifier: Modifier = Modifier,
    isEditAllowed: Boolean,
    contents: ItemContents.Login,
    totpUiState: TotpUiState,
    customFieldsState: CustomFieldsState,
    showCreateAliasButton: Boolean,
    primaryEmail: String?,
    isUpdate: Boolean,
    isTotpError: Boolean,
    focusLastWebsite: Boolean,
    canUpdateUsername: Boolean,
    websitesWithErrors: ImmutableList<Int>,
    onEvent: (LoginContentEvent) -> Unit,
    onGeneratePasswordClick: () -> Unit,
    onCreateAliasClick: () -> Unit,
    onAliasOptionsClick: () -> Unit,
    onNavigate: (BaseLoginNavigation) -> Unit,
    titleSection: @Composable ColumnScope.() -> Unit
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
            titleSection()
            MainLoginSection(
                contents = contents,
                canUpdateUsername = canUpdateUsername,
                totpUiState = totpUiState,
                isEditAllowed = isEditAllowed,
                isTotpError = isTotpError,
                onEvent = onEvent,
                onAliasOptionsClick = onAliasOptionsClick,
                onFocusChange = { field, isFocused ->
                    currentStickyFormOption = if (!isFocused) {
                        None
                    } else {
                        when (field) {
                            MainLoginField.Username -> AliasOptions
                            MainLoginField.Password -> GeneratePassword
                            MainLoginField.Totp -> AddTotp
                        }
                    }
                    onEvent(LoginContentEvent.OnFocusChange(field, isFocused))
                },
                onUpgrade = { onNavigate(BaseLoginNavigation.Upgrade) }
            )
            WebsitesSection(
                websites = contents.urls.toImmutableList(),
                isEditAllowed = isEditAllowed,
                websitesWithErrors = websitesWithErrors,
                focusLastWebsite = focusLastWebsite,
                onWebsiteSectionEvent = { onEvent(LoginContentEvent.OnWebsiteEvent(it)) }
            )
            SimpleNoteSection(
                value = contents.note,
                enabled = isEditAllowed,
                onChange = { onEvent(LoginContentEvent.OnNoteChange(it)) }
            )
            if (isUpdate) {
                LinkedAppsListSection(
                    packageInfoUiSet = contents.packageInfoSet.map(::PackageInfoUi)
                        .toImmutableSet(),
                    isEditable = true,
                    onLinkedAppDelete = { onEvent(LoginContentEvent.OnLinkedAppDelete(it)) }
                )
            }

            CustomFieldsContent(
                state = customFieldsState,
                canEdit = isEditAllowed,
                onEvent = { onEvent(LoginContentEvent.OnCustomFieldEvent(it)) }
            )

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
                        onEvent(LoginContentEvent.OnUsernameChange(it))
                        keyboardController?.hide()
                    }
                )

                AddTotp -> StickyTotpOptions(
                    onPasteCode = {
                        onEvent(LoginContentEvent.PasteTotp)
                        keyboardController?.hide()
                    },
                    onScanCode = {
                        onNavigate(BaseLoginNavigation.ScanTotp)
                        keyboardController?.hide()
                    }
                )

                None -> {}
            }
        }
    }
}

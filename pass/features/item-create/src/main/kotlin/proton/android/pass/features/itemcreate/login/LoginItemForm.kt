/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.features.itemcreate.login

import android.content.pm.PackageManager
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.some
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.composecomponents.impl.attachments.AttachmentSection
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.SimpleNoteSection
import proton.android.pass.composecomponents.impl.form.TitleSection
import proton.android.pass.composecomponents.impl.item.LinkedAppsListSection
import proton.android.pass.composecomponents.impl.utils.passItemColors
import proton.android.pass.domain.ItemDiffs
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.features.itemcreate.login.LoginContentEvent.OnAttachmentEvent
import proton.android.pass.features.itemcreate.login.LoginContentEvent.OnCreateAlias
import proton.android.pass.features.itemcreate.login.LoginContentEvent.OnCreatePassword
import proton.android.pass.features.itemcreate.login.LoginContentEvent.OnCustomFieldEvent
import proton.android.pass.features.itemcreate.login.LoginContentEvent.OnEmailChanged
import proton.android.pass.features.itemcreate.login.LoginContentEvent.OnFocusChange
import proton.android.pass.features.itemcreate.login.LoginContentEvent.OnLinkedAppDelete
import proton.android.pass.features.itemcreate.login.LoginContentEvent.OnNoteChange
import proton.android.pass.features.itemcreate.login.LoginContentEvent.OnScanTotp
import proton.android.pass.features.itemcreate.login.LoginContentEvent.OnTitleChange
import proton.android.pass.features.itemcreate.login.LoginContentEvent.OnWebsiteEvent
import proton.android.pass.features.itemcreate.login.LoginContentEvent.PasteTotp
import proton.android.pass.features.itemcreate.login.LoginStickyFormOptionsContentType.AddTotp
import proton.android.pass.features.itemcreate.login.LoginStickyFormOptionsContentType.AliasOptions
import proton.android.pass.features.itemcreate.login.LoginStickyFormOptionsContentType.GeneratePassword
import proton.android.pass.features.itemcreate.login.LoginStickyFormOptionsContentType.NoOption
import proton.android.pass.features.itemcreate.login.customfields.CustomFieldsContent
import proton.android.pass.features.itemcreate.login.passkey.PasskeyEditRow
import proton.android.pass.features.itemcreate.login.passkey.PasskeysSection

@Composable
internal fun LoginItemForm(
    modifier: Modifier = Modifier,
    loginItemFormState: LoginItemFormState,
    passkeyState: Option<CreatePasskeyState>,
    canUseCustomFields: Boolean,
    isEditAllowed: Boolean,
    totpUiState: TotpUiState,
    focusedField: LoginField?,
    customFieldValidationErrors: ImmutableList<LoginItemValidationErrors.CustomFieldValidationError>,
    showCreateAliasButton: Boolean,
    primaryEmail: String?,
    isUpdate: Boolean,
    isTitleError: Boolean,
    isTotpError: Boolean,
    focusLastWebsite: Boolean,
    canUpdateUsername: Boolean,
    websitesWithErrors: ImmutableList<Int>,
    selectedShareId: ShareId?,
    hasReachedAliasLimit: Boolean,
    isUsernameSplitTooltipEnabled: Boolean,
    isFileAttachmentsEnabled: Boolean,
    attachmentsState: AttachmentsState,
    onEvent: (LoginContentEvent) -> Unit
) {
    Box(modifier = modifier) {
        val currentStickyFormOption = when (focusedField) {
            LoginField.Email -> AliasOptions
            LoginField.Username -> NoOption
            LoginField.Password -> GeneratePassword
            LoginField.PrimaryTotp,
            is LoginCustomField.CustomFieldTOTP -> AddTotp

            is LoginCustomField.CustomFieldHidden,
            is LoginCustomField.CustomFieldText,
            LoginField.Title,
            null -> NoOption
        }

        val isCurrentStickyVisible = currentStickyFormOption != NoOption

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(all = Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
        ) {
            TitleSection(
                modifier = Modifier
                    .roundedContainerNorm()
                    .padding(
                        start = Spacing.medium,
                        top = Spacing.medium,
                        end = Spacing.extraSmall,
                        bottom = Spacing.medium
                    ),
                value = loginItemFormState.title,
                requestFocus = true,
                onTitleRequiredError = isTitleError,
                enabled = isEditAllowed,
                isRounded = true,
                onChange = { onEvent(OnTitleChange(it)) }
            )

            Spacer(modifier = Modifier.height(height = Spacing.extraSmall))

            if (passkeyState is Some) {
                PasskeyEditRow(
                    domain = passkeyState.value.domain,
                    username = passkeyState.value.username,
                    canDelete = false,
                    onDeleteClick = {}
                )
            }

            if (loginItemFormState.hasPasskeys) {
                PasskeysSection(
                    passkeys = loginItemFormState.passkeys.toImmutableList(),
                    onEvent = onEvent
                )
            }

            MainLoginSection(
                loginItemFormState = loginItemFormState,
                canUpdateUsername = canUpdateUsername,
                selectedShareId = selectedShareId,
                totpUiState = totpUiState,
                isEditAllowed = isEditAllowed,
                isTotpError = isTotpError,
                hasReachedAliasLimit = hasReachedAliasLimit,
                onEvent = onEvent,
                onFocusChange = { field, isFocused ->
                    onEvent(OnFocusChange(field, isFocused))
                },
                isUsernameSplitTooltipEnabled = isUsernameSplitTooltipEnabled
            )

            WebsitesSection(
                websites = loginItemFormState.urls.toImmutableList(),
                isEditAllowed = isEditAllowed,
                websitesWithErrors = websitesWithErrors,
                focusLastWebsite = focusLastWebsite,
                onWebsiteSectionEvent = { onEvent(OnWebsiteEvent(it)) }
            )

            SimpleNoteSection(
                value = loginItemFormState.note,
                enabled = isEditAllowed,
                onChange = { onEvent(OnNoteChange(it)) }
            )

            if (isFileAttachmentsEnabled) {
                AttachmentSection(
                    attachmentsState = attachmentsState,
                    isDetail = false,
                    itemColors = passItemColors(ItemCategory.Login),
                    itemDiffs = ItemDiffs.None,
                    onEvent = { onEvent(OnAttachmentEvent(it)) }
                )
            }

            CustomFieldsContent(
                customFields = loginItemFormState.customFields.toImmutableList(),
                focusedField = focusedField as? LoginCustomField,
                canEdit = isEditAllowed,
                canUseCustomFields = canUseCustomFields,
                validationErrors = customFieldValidationErrors,
                onEvent = { onEvent(OnCustomFieldEvent(it)) }
            )

            if (isUpdate) {
                LinkedAppsListSection(
                    packageInfoUiSet = loginItemFormState.packageInfoSet.toImmutableSet(),
                    isEditable = true,
                    onLinkedAppDelete = { onEvent(OnLinkedAppDelete(it)) }
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
                        onClick = { onEvent(OnCreatePassword) }
                    )

                AliasOptions -> StickyUsernameOptions(
                    showCreateAliasButton = showCreateAliasButton,
                    primaryEmail = primaryEmail,
                    onCreateAliasClick = {
                        selectedShareId ?: return@StickyUsernameOptions
                        onEvent(
                            OnCreateAlias(
                                shareId = selectedShareId,
                                hasReachedAliasLimit = hasReachedAliasLimit,
                                title = loginItemFormState.title.some()
                            )
                        )
                    },
                    onPrefillCurrentEmailClick = { prefillEmail ->
                        onEvent(OnEmailChanged(prefillEmail))
                    }
                )

                AddTotp -> {
                    val context = LocalContext.current
                    val hasCamera = remember(LocalContext.current) {
                        context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
                    }

                    StickyTotpOptions(
                        hasCamera = hasCamera,
                        onPasteCode = {
                            onEvent(PasteTotp)
                        },
                        onScanCode = {
                            val index = (focusedField as? LoginCustomField)?.index
                            onEvent(OnScanTotp(index.toOption()))
                        }
                    )
                }

                NoOption -> {}
            }
        }
    }
}

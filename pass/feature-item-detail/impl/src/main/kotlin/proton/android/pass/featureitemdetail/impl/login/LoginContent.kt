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

package proton.android.pass.featureitemdetail.impl.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentSet
import proton.android.pass.common.api.None
import proton.android.pass.common.api.toOption
import proton.android.pass.commonrust.api.PasswordScore
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.commonuimodels.api.UIPasskeyContent
import proton.android.pass.composecomponents.impl.attachments.AttachmentSection
import proton.android.pass.composecomponents.impl.item.LinkedAppsListSection
import proton.android.pass.composecomponents.impl.item.details.sections.login.passkeys.PasskeysSection
import proton.android.pass.composecomponents.impl.item.details.sections.shared.PassItemDetailsHistorySection
import proton.android.pass.composecomponents.impl.item.details.sections.shared.PassItemDetailsMoreInfoSection
import proton.android.pass.composecomponents.impl.utils.passItemColors
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.Vault
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.featureitemdetail.impl.common.NoteSection
import proton.android.pass.featureitemdetail.impl.login.customfield.CustomFieldDetails

@Composable
internal fun LoginContent(
    modifier: Modifier = Modifier,
    itemUiModel: ItemUiModel,
    passwordScore: PasswordScore?,
    vault: Vault?,
    totpUiState: TotpUiState?,
    showViewAlias: Boolean,
    canLoadExternalImages: Boolean,
    customFields: ImmutableList<CustomFieldUiContent>,
    isHistoryFeatureEnabled: Boolean,
    isFileAttachmentsEnabled: Boolean,
    passkeys: ImmutableList<UIPasskeyContent>,
    monitorState: LoginMonitorState,
    onEvent: (LoginDetailEvent) -> Unit
) {
    val contents = itemUiModel.contents as ItemContents.Login

    Column(
        modifier = modifier.padding(horizontal = Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.mediumSmall)
    ) {
        AnimatedVisibility(visible = monitorState.shouldDisplayMonitoring) {
            LoginMonitorSection(
                modifier = Modifier.padding(top = Spacing.small),
                monitorState = monitorState,
                canLoadExternalImages = canLoadExternalImages,
                onEvent = onEvent
            )
        }

        LoginTitle(
            modifier = Modifier.padding(Spacing.none, Spacing.mediumSmall),
            title = itemUiModel.contents.title,
            vault = vault,
            website = contents.urls.firstOrNull(),
            packageName = contents.packageInfoSet.minByOrNull { it.packageName.value }?.packageName?.value,
            canLoadExternalImages = canLoadExternalImages,
            onVaultClick = { onEvent(LoginDetailEvent.OnVaultClick) },
            isPinned = itemUiModel.isPinned
        )

        if (passkeys.isNotEmpty()) {
            PasskeysSection(
                passkeys = passkeys,
                itemColors = passItemColors(itemCategory = ItemCategory.Login),
                onSelected = {
                    onEvent(LoginDetailEvent.OnSelectPasskey(it))
                }
            )
        }

        MainLoginSection(
            email = contents.itemEmail,
            username = contents.itemUsername,
            passwordHiddenState = contents.password,
            passwordScore = passwordScore,
            totpUiState = totpUiState,
            showViewAlias = showViewAlias,
            onEvent = onEvent
        )

        if (contents.urls.isNotEmpty()) {
            WebsiteSection(
                websites = contents.urls.toPersistentList(),
                onEvent = onEvent
            )
        }

        if (contents.note.isNotEmpty()) {
            NoteSection(
                text = contents.note,
                accentColor = PassTheme.colors.loginInteractionNorm
            )
        }

        if (customFields.isNotEmpty()) {
            CustomFieldDetails(
                fields = customFields,
                onEvent = { onEvent(LoginDetailEvent.OnCustomFieldEvent(it)) }
            )
        }

        if (isFileAttachmentsEnabled) {
            AttachmentSection(
                files = emptyList(),
                isDetail = true,
                colors = passItemColors(ItemCategory.Login),
                loadingFile = None,
                onAttachmentOptions = {},
                onAttachmentOpen = {},
                onAddAttachment = {},
                onTrashAll = {}
            )
        }

        PassItemDetailsHistorySection(
            lastAutofillAtOption = itemUiModel.lastAutofillTime.toOption(),
            revision = itemUiModel.revision,
            createdAt = itemUiModel.createTime,
            modifiedAt = itemUiModel.modificationTime,
            onViewItemHistoryClicked = { onEvent(LoginDetailEvent.OnViewItemHistoryClicked) },
            itemColors = passItemColors(itemCategory = ItemCategory.Login),
            shouldDisplayItemHistoryButton = isHistoryFeatureEnabled
        )

        LinkedAppsListSection(
            packageInfoUiSet = contents.packageInfoSet.map { PackageInfoUi(it) }.toPersistentSet(),
            isEditable = false,
            onLinkedAppDelete = {}
        )

        PassItemDetailsMoreInfoSection(
            itemId = itemUiModel.id,
            shareId = itemUiModel.shareId
        )
    }
}

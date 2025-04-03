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

package proton.android.pass.features.itemdetail.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentSet
import proton.android.pass.common.api.toOption
import proton.android.pass.commonrust.api.PasswordScore
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.commonuimodels.api.UIPasskeyContent
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.composecomponents.impl.attachments.AttachmentSection
import proton.android.pass.composecomponents.impl.item.LinkedAppsListSection
import proton.android.pass.composecomponents.impl.item.details.sections.login.passkeys.PasskeysSection
import proton.android.pass.composecomponents.impl.item.details.sections.shared.PassItemDetailsHistorySection
import proton.android.pass.composecomponents.impl.item.details.sections.shared.PassItemDetailsMoreInfoSection
import proton.android.pass.composecomponents.impl.utils.passItemColors
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.Share
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.features.itemdetail.common.NoteSection
import proton.android.pass.features.itemdetail.login.customfield.CustomFieldDetails

@Composable
internal fun LoginContent(
    modifier: Modifier = Modifier,
    itemUiModel: ItemUiModel,
    passwordScore: PasswordScore?,
    share: Share,
    totpUiState: TotpUiState?,
    showViewAlias: Boolean,
    canLoadExternalImages: Boolean,
    customFields: ImmutableList<CustomFieldUiContent>,
    canViewItemHistory: Boolean,
    isFileAttachmentsEnabled: Boolean,
    passkeys: ImmutableList<UIPasskeyContent>,
    monitorState: LoginMonitorState,
    attachmentsState: AttachmentsState,
    hasMoreThanOneVaultShare: Boolean,
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
            share = share,
            website = contents.urls.firstOrNull(),
            packageName = contents.packageInfoSet.minByOrNull { it.packageName.value }?.packageName?.value,
            canLoadExternalImages = canLoadExternalImages,
            onShareClick = { onEvent(LoginDetailEvent.OnShareClick) },
            isPinned = itemUiModel.isPinned,
            hasMoreThanOneVaultShare = hasMoreThanOneVaultShare
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
                attachmentsState = attachmentsState,
                isDetail = true,
                itemColors = passItemColors(ItemCategory.Login),
                onEvent = { onEvent(LoginDetailEvent.OnAttachmentEvent(it)) }
            )
        }

        PassItemDetailsHistorySection(
            lastAutofillAtOption = itemUiModel.lastAutofillTime.toOption(),
            revision = itemUiModel.revision,
            createdAt = itemUiModel.createTime,
            modifiedAt = itemUiModel.modificationTime,
            onViewItemHistoryClicked = { onEvent(LoginDetailEvent.OnViewItemHistoryClicked) },
            itemColors = passItemColors(itemCategory = ItemCategory.Login),
            shouldDisplayItemHistoryButton = canViewItemHistory
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

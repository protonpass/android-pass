/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.composecomponents.impl.item.details.sections.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.datetime.Instant
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.PasswordStrength
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.commonuimodels.api.UIPasskeyContent
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.composecomponents.impl.attachments.AttachmentSection
import proton.android.pass.composecomponents.impl.item.details.PassItemDetailsUiEvent
import proton.android.pass.composecomponents.impl.item.details.sections.login.passkeys.PasskeysSection
import proton.android.pass.composecomponents.impl.item.details.sections.shared.PassItemDetailCustomFieldsSection
import proton.android.pass.composecomponents.impl.item.details.sections.shared.PassItemDetailsHistorySection
import proton.android.pass.composecomponents.impl.item.details.sections.shared.PassItemDetailsMoreInfoSection
import proton.android.pass.composecomponents.impl.item.details.sections.shared.PassSharedItemDetailNoteSection
import proton.android.pass.composecomponents.impl.utils.PassItemColors
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemDiffs
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.TotpState
import proton.android.pass.domain.VaultId

@Composable
internal fun PassLoginItemDetailSections(
    modifier: Modifier = Modifier,
    itemId: ItemId,
    shareId: ShareId,
    vaultId: VaultId,
    contents: ItemContents.Login,
    passwordStrength: PasswordStrength,
    primaryTotp: TotpState?,
    customFieldTotps: ImmutableMap<Pair<Option<Int>, Int>, TotpState>,
    passkeys: ImmutableList<UIPasskeyContent>,
    itemColors: PassItemColors,
    itemDiffs: ItemDiffs.Login,
    lastAutofillOption: Option<Instant>,
    revision: Long,
    createdAt: Instant,
    modifiedAt: Instant,
    shouldDisplayItemHistorySection: Boolean,
    shouldDisplayItemHistoryButton: Boolean,
    shouldDisplayFileAttachments: Boolean,
    attachmentsState: AttachmentsState,
    onEvent: (PassItemDetailsUiEvent) -> Unit
) = with(contents) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = Spacing.medium.minus(Spacing.extraSmall))
    ) {
        if (passkeys.isNotEmpty()) {
            PasskeysSection(
                passkeys = passkeys,
                itemDiffs = itemDiffs,
                itemColors = itemColors,
                onSelected = { passkeyContent ->
                    onEvent(PassItemDetailsUiEvent.OnPasskeyClick(passkeyContent))
                }
            )
        }

        PassLoginItemDetailMainSection(
            email = itemEmail,
            username = itemUsername,
            password = password,
            passwordStrength = passwordStrength,
            primaryTotp = primaryTotp,
            itemColors = itemColors,
            itemDiffs = itemDiffs,
            onEvent = onEvent
        )

        if (urls.isNotEmpty()) {
            PassLoginItemDetailWebsitesSection(
                websiteUrls = urls.toPersistentList(),
                itemColors = itemColors,
                itemDiffs = itemDiffs,
                onEvent = onEvent
            )
        }

        if (note.isNotBlank()) {
            PassSharedItemDetailNoteSection(
                note = note,
                itemColors = itemColors,
                itemDiffs = itemDiffs
            )
        }

        if (customFields.isNotEmpty()) {
            PassItemDetailCustomFieldsSection(
                customFields = customFields.toPersistentList(),
                customFieldTotps = customFieldTotps,
                itemColors = itemColors,
                itemDiffs = itemDiffs,
                onEvent = onEvent
            )
        }

        if (shouldDisplayFileAttachments) {
            AttachmentSection(
                attachmentsState = attachmentsState,
                isDetail = true,
                itemColors = itemColors,
                itemDiffs = itemDiffs.attachments,
                onEvent = { onEvent(PassItemDetailsUiEvent.OnAttachmentEvent(it)) }
            )
        }

        if (shouldDisplayItemHistorySection) {
            PassItemDetailsHistorySection(
                lastAutofillAtOption = lastAutofillOption,
                revision = revision,
                createdAt = createdAt,
                modifiedAt = modifiedAt,
                itemColors = itemColors,
                onViewItemHistoryClicked = { onEvent(PassItemDetailsUiEvent.OnViewItemHistoryClick) },
                shouldDisplayItemHistoryButton = shouldDisplayItemHistoryButton
            )
        }

        if (packageInfoSet.isNotEmpty()) {
            val mapped = remember(packageInfoSet.hashCode()) {
                packageInfoSet.map { PackageInfoUi(it) }.toPersistentSet()
            }

            PassLoginItemDetailLinkedAppsSection(
                packageInfoUiSet = mapped,
                isEditable = false,
                onLinkedAppDelete = {},
                itemDiffs = itemDiffs
            )
        }

        PassItemDetailsMoreInfoSection(
            itemId = itemId,
            shareId = shareId,
            vaultId = vaultId
        )
    }
}

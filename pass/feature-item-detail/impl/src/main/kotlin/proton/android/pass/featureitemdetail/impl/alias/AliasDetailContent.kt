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

package proton.android.pass.featureitemdetail.impl.alias

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.PersistentList
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.SpecialCharacters
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.item.details.sections.shared.PassItemDetailsHistorySection
import proton.android.pass.composecomponents.impl.item.details.sections.shared.PassItemDetailsMoreInfoSection
import proton.android.pass.composecomponents.impl.utils.passItemColors
import proton.android.pass.domain.AliasMailbox
import proton.android.pass.domain.AliasStats
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.Vault
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.featureitemdetail.impl.common.NoteSection
import proton.android.pass.featureitemdetail.impl.common.SenderNameSection

@Composable
fun AliasDetailContent(
    modifier: Modifier = Modifier,
    itemUiModel: ItemUiModel,
    vault: Vault?,
    mailboxes: PersistentList<AliasMailbox>,
    isAliasCreatedByUser: Boolean,
    slNote: String,
    displayName: String,
    stats: Option<AliasStats>,
    contactsCount: Int,
    isLoading: Boolean,
    isAliasSyncEnabled: Boolean,
    isAliasStateToggling: Boolean,
    isHistoryFeatureEnabled: Boolean,
    isAliasManagementEnabled: Boolean,
    onCopyAlias: (String) -> Unit,
    onCreateLoginFromAlias: (String) -> Unit,
    onToggleAliasState: (Boolean) -> Unit,
    onVaultClick: () -> Unit,
    onViewItemHistoryClicked: () -> Unit,
    onContactsClicked: () -> Unit
) {
    val contents = itemUiModel.contents as ItemContents.Alias
    Column(
        modifier = modifier.padding(horizontal = Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.mediumSmall)
    ) {
        AliasTitle(
            modifier = Modifier.padding(Spacing.none, Spacing.mediumSmall),
            title = contents.title,
            isActive = contents.isEnabled,
            vault = vault,
            onVaultClick = onVaultClick,
            isPinned = itemUiModel.isPinned
        )

        AliasSection(
            alias = contents.aliasEmail,
            isAliasEnabled = contents.isEnabled,
            isAliasSyncEnabled = isAliasSyncEnabled,
            isAliasStateToggling = isAliasStateToggling,
            mailboxes = mailboxes,
            isLoading = isLoading,
            onCopyAlias = onCopyAlias,
            onCreateLoginFromAlias = onCreateLoginFromAlias,
            onToggleAliasState = onToggleAliasState
        )

        if (contents.note.isNotBlank()) {
            NoteSection(
                text = contents.note,
                accentColor = PassTheme.colors.aliasInteractionNorm
            )
        }

        if (isAliasManagementEnabled && slNote.isNotBlank()) {
            NoteSection(
                title = buildString {
                    append(stringResource(id = R.string.item_details_shared_section_note_title))
                    append(" ${SpecialCharacters.DOT_SEPARATOR} ")
                    append(stringResource(id = R.string.simple_login_brand_name))
                },
                text = slNote,
                accentColor = PassTheme.colors.aliasInteractionNorm
            )
        }

        if (isAliasManagementEnabled) {
            if (isAliasCreatedByUser) {
                SenderNameSection(
                    text = displayName,
                    isLoading = isLoading
                )

                ContactsSection(
                    modifier = Modifier.padding(bottom = Spacing.small),
                    counter = contactsCount,
                    onClick = onContactsClicked
                )
            }

            if (stats is Some) {
                AliasStats(stats = stats.value)
            }
        }

        PassItemDetailsHistorySection(
            lastAutofillAtOption = itemUiModel.lastAutofillTime.toOption(),
            revision = itemUiModel.revision,
            createdAt = itemUiModel.createTime,
            modifiedAt = itemUiModel.modificationTime,
            onViewItemHistoryClicked = onViewItemHistoryClicked,
            itemColors = passItemColors(itemCategory = ItemCategory.Alias),
            shouldDisplayItemHistoryButton = isHistoryFeatureEnabled
        )

        PassItemDetailsMoreInfoSection(
            itemId = itemUiModel.id,
            shareId = itemUiModel.shareId
        )
    }
}

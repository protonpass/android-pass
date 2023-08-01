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
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.PersistentList
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.featureitemdetail.impl.common.MoreInfo
import proton.android.pass.featureitemdetail.impl.common.MoreInfoUiState
import proton.android.pass.featureitemdetail.impl.common.NoteSection
import proton.pass.domain.AliasMailbox
import proton.pass.domain.ItemContents
import proton.pass.domain.Vault

@Composable
fun AliasDetailContent(
    modifier: Modifier = Modifier,
    itemUiModel: ItemUiModel,
    vault: Vault?,
    moreInfoUiState: MoreInfoUiState,
    mailboxes: PersistentList<AliasMailbox>,
    isLoading: Boolean,
    onCopyAlias: (String) -> Unit,
    onCreateLoginFromAlias: (String) -> Unit,
    onVaultClick: () -> Unit
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AliasTitle(
            modifier = Modifier.padding(0.dp, 12.dp),
            title = itemUiModel.contents.title,
            vault = vault,
            onVaultClick = onVaultClick
        )
        AliasSection(
            alias = (itemUiModel.contents as ItemContents.Alias).aliasEmail,
            mailboxes = mailboxes,
            isLoading = isLoading,
            onCopyAlias = onCopyAlias,
            onCreateLoginFromAlias = onCreateLoginFromAlias
        )
        NoteSection(
            text = itemUiModel.contents.note,
            accentColor = PassTheme.colors.aliasInteractionNorm
        )
        MoreInfo(moreInfoUiState = moreInfoUiState)
    }
}


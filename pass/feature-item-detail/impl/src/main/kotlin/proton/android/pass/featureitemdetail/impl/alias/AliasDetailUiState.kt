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

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.PersistentList
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.featureitemdetail.impl.common.ShareClickAction
import proton.pass.domain.AliasMailbox
import proton.pass.domain.Vault

sealed interface AliasDetailUiState {

    @Stable
    object NotInitialised : AliasDetailUiState

    @Stable
    object Error : AliasDetailUiState

    @Stable
    data class Success(
        val itemUiModel: ItemUiModel,
        val vault: Vault?,
        val mailboxes: PersistentList<AliasMailbox>,
        val isLoading: Boolean,
        val isLoadingMailboxes: Boolean,
        val isItemSentToTrash: Boolean,
        val isPermanentlyDeleted: Boolean,
        val isRestoredFromTrash: Boolean,
        val canMigrate: Boolean,
        val canPerformActions: Boolean,
        val shareClickAction: ShareClickAction
    ) : AliasDetailUiState
}

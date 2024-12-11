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

package proton.android.pass.features.sharing.sharefromitem

import androidx.compose.runtime.Stable
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.data.api.usecases.capabilities.VaultAccessData
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.Share
import proton.android.pass.domain.VaultWithItemCount

@Stable
internal sealed interface ShareFromItemNavEvent {

    @Stable
    data object Unknown : ShareFromItemNavEvent

    @Stable
    data object MoveToSharedVault : ShareFromItemNavEvent

}

@Stable
internal sealed interface CreateNewVaultState {

    @Stable
    data object Allow : CreateNewVaultState

    @Stable
    data object Upgrade : CreateNewVaultState

    @Stable
    data object VaultLimitReached : CreateNewVaultState

    @Stable
    data object Hide : CreateNewVaultState

}

@Stable
internal data class ShareFromItemUiState(
    val vault: Option<VaultWithItemCount>,
    val itemId: ItemId,
    val showMoveToSharedVault: Boolean,
    val showCreateVault: CreateNewVaultState,
    val event: ShareFromItemNavEvent,
    val isSingleSharingAvailable: Boolean,
    val canUsePaidFeatures: Boolean,
    val isItemSharingAvailable: Boolean,
    private val vaultAccessData: VaultAccessData,
    private val shareOption: Option<Share>
) {

    private val isSharedVault: Boolean = when (vault) {
        None -> false
        is Some -> vault.value.vault.shared
    }

    internal val canManageSharedVault: Boolean = isSharedVault && vaultAccessData.canManageAccess

    internal val canViewSharedVaultMembers: Boolean =
        isSharedVault && vaultAccessData.canViewMembers

    internal val sharedVaultMembersCount: Int by lazy {
        when (vault) {
            None -> 0
            is Some -> vault.value.vault.members
        }
    }

    internal val canShareVault: Boolean = when (shareOption) {
        None -> false
        is Some -> when (val share = shareOption.value) {
            is Share.Item -> false
            is Share.Vault -> share.isOwner || share.isAdmin
        }
    }

    internal companion object {

        fun initial(itemId: ItemId) = ShareFromItemUiState(
            vault = None,
            itemId = itemId,
            showMoveToSharedVault = false,
            showCreateVault = CreateNewVaultState.Hide,
            event = ShareFromItemNavEvent.Unknown,
            isSingleSharingAvailable = false,
            canUsePaidFeatures = false,
            isItemSharingAvailable = false,
            vaultAccessData = VaultAccessData(
                canManageAccess = false,
                canViewMembers = false
            ),
            shareOption = None
        )

    }

}

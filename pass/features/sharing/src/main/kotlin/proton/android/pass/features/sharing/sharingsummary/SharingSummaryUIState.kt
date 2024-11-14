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

package proton.android.pass.features.sharing.sharingsummary

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.repositories.AddressPermission
import proton.android.pass.domain.Item
import proton.android.pass.domain.Vault
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.features.sharing.common.AddressPermissionUiState
import proton.android.pass.features.sharing.common.toUiState

@Immutable
internal sealed interface SharingSummaryEvent {

    @Immutable
    data object Idle : SharingSummaryEvent

    @Immutable
    data object BackToHome : SharingSummaryEvent

    @Immutable
    data object Shared : SharingSummaryEvent

    @Immutable
    data object Error : SharingSummaryEvent

}

@Immutable
internal data class SharingSummaryUIState(
    val addresses: ImmutableList<AddressPermissionUiState> = persistentListOf(),
    val vaultWithItemCount: VaultWithItemCount? = null,
    val isLoading: Boolean = false,
    val event: SharingSummaryEvent = SharingSummaryEvent.Idle
)

internal sealed interface SharingSummaryState {

    val addresses: ImmutableList<AddressPermissionUiState>

    val isLoading: Boolean

    val event: SharingSummaryEvent

    data object Initial : SharingSummaryState {

        override val addresses: ImmutableList<AddressPermissionUiState> = persistentListOf()

        override val isLoading: Boolean = false

        override val event: SharingSummaryEvent = SharingSummaryEvent.Idle

    }

    data class ShareItem(
        override val event: SharingSummaryEvent,
        private val addressPermissions: List<AddressPermission>,
        private val isLoadingState: IsLoadingState,
        private val item: Item
    ) : SharingSummaryState {

        override val addresses: ImmutableList<AddressPermissionUiState> = addressPermissions
            .map { addressPermission -> addressPermission.toUiState() }
            .toPersistentList()

        override val isLoading: Boolean = isLoadingState.value()

        internal val itemTitle: String = item.title

    }

    data class ShareVault(
        override val event: SharingSummaryEvent,
        private val vaultWithItemCount: VaultWithItemCount,
        private val addressPermissions: List<AddressPermission>,
        private val isLoadingState: IsLoadingState
    ) : SharingSummaryState {

        override val addresses: ImmutableList<AddressPermissionUiState> = addressPermissions
            .map { addressPermission -> addressPermission.toUiState() }
            .toPersistentList()

        override val isLoading: Boolean = isLoadingState.value()

        internal val vault: Vault = vaultWithItemCount.vault

        internal val vaultItemCount: Int = vaultWithItemCount.activeItemCount
            .plus(vaultWithItemCount.trashedItemCount)
            .toInt()

    }

}

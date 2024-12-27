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

package proton.android.pass.features.migrate.selectvault

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList
import proton.android.pass.common.api.Option
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.VaultWithItemCount

sealed interface SelectVaultEvent {
    data class VaultSelectedForMigrateItem(
        val destinationShareId: ShareId
    ) : SelectVaultEvent

    data class VaultSelectedForMigrateAll(
        val sourceShareId: ShareId,
        val destinationShareId: ShareId
    ) : SelectVaultEvent

    data object Close : SelectVaultEvent
}

enum class MigrateMode {
    MigrateItem,
    MigrateAll
}

@Stable
sealed interface VaultStatus {
    @Stable
    data object Enabled : VaultStatus

    @JvmInline
    @Stable
    value class Disabled(val reason: DisabledReason) : VaultStatus

    @Stable
    sealed interface DisabledReason {
        @Stable
        data object NoPermission : DisabledReason

        @Stable
        data object SameVault : DisabledReason
    }
}

data class VaultEnabledPair(
    val vault: VaultWithItemCount,
    val status: VaultStatus
)

sealed class MigrateSelectVaultUiState {
    @Stable
    data object Uninitialised : MigrateSelectVaultUiState()

    @Stable
    data object Loading : MigrateSelectVaultUiState()

    @Stable
    data object Error : MigrateSelectVaultUiState()

    @Stable
    data class Success(
        val vaultList: ImmutableList<VaultEnabledPair>,
        val event: Option<SelectVaultEvent>,
        val mode: MigrateMode
    ) : MigrateSelectVaultUiState()
}

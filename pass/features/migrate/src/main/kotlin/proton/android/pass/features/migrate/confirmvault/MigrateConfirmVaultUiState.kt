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

package proton.android.pass.features.migrate.confirmvault

import androidx.compose.runtime.Stable
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.VaultWithItemCount

internal sealed interface ConfirmMigrateEvent {

    data object Close : ConfirmMigrateEvent

    data class ItemMigrated(val shareId: ShareId, val itemId: ItemId) : ConfirmMigrateEvent

    data object AllItemsMigrated : ConfirmMigrateEvent

}

@Stable
internal sealed interface MigrateMode {

    @Stable
    @JvmInline
    value class MigrateSelectedItems(val number: Int) : MigrateMode

    @Stable
    data object MigrateAll : MigrateMode

}

@Stable
internal data class MigrateConfirmVaultUiState(
    val isLoading: IsLoadingState,
    val event: Option<ConfirmMigrateEvent>,
    val vault: Option<VaultWithItemCount>,
    val mode: MigrateMode,
    val hasAssociatedSecureLinks: Boolean
) {

    internal companion object {

        internal fun initial(mode: MigrateMode) = MigrateConfirmVaultUiState(
            isLoading = IsLoadingState.NotLoading,
            event = None,
            vault = None,
            mode = mode,
            hasAssociatedSecureLinks = false
        )

    }

}

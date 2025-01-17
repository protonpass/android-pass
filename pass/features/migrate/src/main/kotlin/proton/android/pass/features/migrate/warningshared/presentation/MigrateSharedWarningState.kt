/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.migrate.warningshared.presentation

import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.domain.items.MigrationItemsSelection

internal data class MigrateSharedWarningState(
    internal val event: MigrateSharedWarningEvent,
    private val isLoadingState: IsLoadingState,
    private val migrationItemsSelection: MigrationItemsSelection
) {

    internal val isLoading: Boolean = isLoadingState.value()

    internal val totalItemsCount: Int = migrationItemsSelection.itemsCount

    internal val sharedItemsCount: Int = migrationItemsSelection.sharedItemsCount

    internal val isSingleSharedItem: Boolean = totalItemsCount == 1

    internal companion object {

        internal val Initial: MigrateSharedWarningState = MigrateSharedWarningState(
            event = MigrateSharedWarningEvent.Idle,
            isLoadingState = IsLoadingState.NotLoading,
            migrationItemsSelection = MigrationItemsSelection(
                items = emptyList()
            )
        )

    }

}

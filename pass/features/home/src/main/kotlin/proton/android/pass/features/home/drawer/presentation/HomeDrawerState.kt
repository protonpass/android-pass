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

package proton.android.pass.features.home.drawer.presentation

import androidx.compose.runtime.Stable
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.data.api.ItemCountSummary
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.searchoptions.api.VaultSelectionOption

@Stable
internal data class HomeDrawerState(
    internal val vaultShares: List<VaultWithItemCount>,
    internal val canCreateVaults: Boolean,
    internal val canOrganiseVaults: Boolean,
    internal val vaultSelectionOption: VaultSelectionOption,
    private val itemCountSummaryOption: Option<ItemCountSummary>
) {

    internal val sharedWithMeItemsCount: Int = when (itemCountSummaryOption) {
        None -> 0
        is Some ->
            itemCountSummaryOption
                .value
                .sharedWithMe
                .toInt()
    }

    internal val hasSharedWithMeItems: Boolean = sharedWithMeItemsCount > 0

    internal val sharedByMeItemsCount: Int = when (itemCountSummaryOption) {
        None -> 0
        is Some ->
            itemCountSummaryOption
                .value
                .sharedByMe
                .toInt()
    }

    internal val hasSharedByMeItems: Boolean = sharedByMeItemsCount > 0

    private val sharedWithMeActiveItemsCount: Int = when (itemCountSummaryOption) {
        None -> 0
        is Some ->
            itemCountSummaryOption
                .value
                .sharedWithMeActive
                .toInt()
    }

    internal val allItemsCount: Int = vaultShares.sumOf { it.activeItemCount }
        .plus(sharedWithMeActiveItemsCount)
        .toInt()

    internal val trashedItemsCount: Int = itemCountSummaryOption.value()
        ?.trashed
        ?.toInt()
        ?: 0

    internal companion object {

        internal val Initial: HomeDrawerState = HomeDrawerState(
            vaultShares = emptyList(),
            canCreateVaults = false,
            canOrganiseVaults = false,
            vaultSelectionOption = VaultSelectionOption.AllVaults,
            itemCountSummaryOption = None
        )

    }

}

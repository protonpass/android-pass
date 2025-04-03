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

package proton.android.pass.features.searchoptions

import androidx.compose.runtime.Stable
import proton.android.pass.data.api.ItemCountSummary
import proton.android.pass.searchoptions.api.SearchFilterType
import proton.android.pass.searchoptions.api.SearchOptions
import proton.android.pass.searchoptions.api.VaultSelectionOption

@Stable
internal sealed interface FilterOptionsState {

    @Stable
    data object Empty : FilterOptionsState

    @Stable
    data class Success(
        internal val summary: ItemCountSummary,
        internal val isCustomItemEnabled: Boolean,
        private val searchOptions: SearchOptions
    ) : FilterOptionsState {

        internal val filterType: SearchFilterType = searchOptions.filterOption.searchFilterType

        private val isShareByOrWithMeFilterAvailable: Boolean =
            when (searchOptions.vaultSelectionOption) {
                VaultSelectionOption.AllVaults,
                VaultSelectionOption.Trash,
                is VaultSelectionOption.Vault -> true

                VaultSelectionOption.SharedByMe,
                VaultSelectionOption.SharedWithMe -> false
            }


        internal val isSharedByMeFilterAvailable: Boolean = summary.hasSharedByMeItems
            .and(isShareByOrWithMeFilterAvailable)

        internal val isSharedWithMeFilterAvailable: Boolean = summary.hasSharedWithMeItems
            .and(isShareByOrWithMeFilterAvailable)

    }

}

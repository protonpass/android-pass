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

package proton.android.featuresearchoptions.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import proton.android.pass.featuresearchoptions.api.HomeSearchOptionsRepository
import proton.android.pass.featuresearchoptions.api.SearchOptions
import proton.android.pass.featuresearchoptions.api.SortingOption
import proton.android.pass.featuresearchoptions.api.VaultSelectionOption
import proton.android.pass.preferences.InternalSettingsRepository
import proton.android.pass.preferences.SelectedVaultPreference
import proton.android.pass.preferences.SortingOptionPreference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeSearchOptionsRepositoryImpl @Inject constructor(
    private val internalSettingsRepository: InternalSettingsRepository
) : HomeSearchOptionsRepository {

    override fun observeSearchOptions(): Flow<SearchOptions> = combine(
        internalSettingsRepository.getHomeSortingOption(),
        internalSettingsRepository.getSelectedVault()
    ) { sorting, vault ->
        SearchOptions(
            sortingOption = SortingOption(sorting.toDomain()),
            vaultSelectionOption = vault.toSelectionOption()
        )
    }

    override fun observeSortingOption(): Flow<SortingOption> = internalSettingsRepository
        .getHomeSortingOption()
        .map { SortingOption(it.toDomain()) }

    override fun observeVaultSelectionOption(): Flow<VaultSelectionOption> = internalSettingsRepository
        .getSelectedVault()
        .map { it.toSelectionOption() }


    override fun setSortingOption(sortingOption: SortingOption) {
        internalSettingsRepository.setHomeSortingOption(sortingOption.toPreference())
    }

    override fun setVaultSelectionOption(vaultSelectionOption: VaultSelectionOption) {
        internalSettingsRepository.setSelectedVault(vaultSelectionOption.toPreference())
    }

    override fun clearSearchOptions() {
        internalSettingsRepository.setHomeSortingOption(SortingOptionPreference.MostRecent)
        internalSettingsRepository.setSelectedVault(SelectedVaultPreference.AllVaults)
    }
}

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

package proton.android.pass.searchoptions.impl

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.preferences.InternalSettingsRepository
import proton.android.pass.searchoptions.api.FilterOption
import proton.android.pass.searchoptions.api.HomeSearchOptionsRepository
import proton.android.pass.searchoptions.api.SearchOptions
import proton.android.pass.searchoptions.api.SortingOption
import proton.android.pass.searchoptions.api.VaultSelectionOption
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeSearchOptionsRepositoryImpl @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUser,
    private val internalSettingsRepository: InternalSettingsRepository
) : HomeSearchOptionsRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val selectedVaultFlow = observeCurrentUser()
        .flatMapLatest { user ->
            internalSettingsRepository.getSelectedVault(user.userId)
                .map { selectedVault -> user.userId to selectedVault }
        }
        .distinctUntilChanged()

    override fun observeSearchOptions(): Flow<SearchOptions> = combine(
        internalSettingsRepository.getHomeFilterOption(),
        internalSettingsRepository.getHomeSortingOption(),
        selectedVaultFlow
    ) { filter, sorting, (userId, vault) ->
        SearchOptions(
            filterOption = FilterOption(filter.toDomain()),
            sortingOption = SortingOption(sorting.toDomain()),
            vaultSelectionOption = vault.toSelectionOption(),
            userId = userId
        )
    }

    override fun observeSortingOption(): Flow<SortingOption> = internalSettingsRepository
        .getHomeSortingOption()
        .map { SortingOption(it.toDomain()) }

    override fun observeFilterOption(): Flow<FilterOption> = internalSettingsRepository.getHomeFilterOption()
        .map { FilterOption(it.toDomain()) }

    override fun observeVaultSelectionOption(): Flow<VaultSelectionOption> = selectedVaultFlow
        .map { it.second.toSelectionOption() }

    override fun setSortingOption(sortingOption: SortingOption) {
        internalSettingsRepository.setHomeSortingOption(sortingOption.toPreference())
    }

    override fun setFilterOption(filterOption: FilterOption) {
        internalSettingsRepository.setHomeFilterOption(filterOption.toPreference())
    }

    override suspend fun setVaultSelectionOption(vaultSelectionOption: VaultSelectionOption) {
        observeCurrentUser().firstOrNull()?.also { user ->
            internalSettingsRepository.setSelectedVault(
                userId = user.userId,
                selectedVault = vaultSelectionOption.toPreference()
            )
        }
    }

}

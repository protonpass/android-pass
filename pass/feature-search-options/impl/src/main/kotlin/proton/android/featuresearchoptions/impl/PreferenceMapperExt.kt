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

import proton.android.pass.featuresearchoptions.api.SearchSortingType
import proton.android.pass.featuresearchoptions.api.SortingOption
import proton.android.pass.featuresearchoptions.api.VaultSelectionOption
import proton.android.pass.preferences.SelectedVaultPreference
import proton.android.pass.preferences.SortingOptionPreference
import proton.android.pass.domain.ShareId

internal fun SortingOptionPreference.toDomain(): SearchSortingType = when (this) {
    SortingOptionPreference.MostRecent -> SearchSortingType.MostRecent
    SortingOptionPreference.TitleAZ -> SearchSortingType.TitleAsc
    SortingOptionPreference.TitleZA -> SearchSortingType.TitleDesc
    SortingOptionPreference.OldestNewest -> SearchSortingType.CreationAsc
    SortingOptionPreference.NewestOldest -> SearchSortingType.CreationDesc
}

internal fun SelectedVaultPreference.toSelectionOption(): VaultSelectionOption = when (this) {
    SelectedVaultPreference.AllVaults -> VaultSelectionOption.AllVaults
    is SelectedVaultPreference.Vault -> VaultSelectionOption.Vault(ShareId(this.shareId))
    SelectedVaultPreference.Trash -> VaultSelectionOption.Trash
}

internal fun SortingOption.toPreference(): SortingOptionPreference = when (this.searchSortingType) {
    SearchSortingType.MostRecent -> SortingOptionPreference.MostRecent
    SearchSortingType.TitleAsc -> SortingOptionPreference.TitleAZ
    SearchSortingType.TitleDesc -> SortingOptionPreference.TitleZA
    SearchSortingType.CreationAsc -> SortingOptionPreference.OldestNewest
    SearchSortingType.CreationDesc -> SortingOptionPreference.NewestOldest
}

internal fun VaultSelectionOption.toPreference(): SelectedVaultPreference = when (this) {
    VaultSelectionOption.AllVaults -> SelectedVaultPreference.AllVaults
    is VaultSelectionOption.Vault -> SelectedVaultPreference.Vault(this.shareId.id)
    VaultSelectionOption.Trash -> SelectedVaultPreference.Trash
}

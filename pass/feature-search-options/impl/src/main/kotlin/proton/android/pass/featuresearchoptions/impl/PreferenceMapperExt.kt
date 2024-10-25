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

package proton.android.pass.featuresearchoptions.impl

import proton.android.pass.domain.ShareId
import proton.android.pass.featuresearchoptions.api.FilterOption
import proton.android.pass.featuresearchoptions.api.SearchFilterType
import proton.android.pass.featuresearchoptions.api.SearchSortingType
import proton.android.pass.featuresearchoptions.api.SortingOption
import proton.android.pass.featuresearchoptions.api.VaultSelectionOption
import proton.android.pass.preferences.FilterOptionPreference
import proton.android.pass.preferences.SelectedVaultPreference
import proton.android.pass.preferences.SortingOptionPreference

internal fun SortingOptionPreference.toDomain(): SearchSortingType = when (this) {
    SortingOptionPreference.MostRecent -> SearchSortingType.MostRecent
    SortingOptionPreference.TitleAZ -> SearchSortingType.TitleAsc
    SortingOptionPreference.TitleZA -> SearchSortingType.TitleDesc
    SortingOptionPreference.OldestNewest -> SearchSortingType.CreationAsc
    SortingOptionPreference.NewestOldest -> SearchSortingType.CreationDesc
}

internal fun FilterOptionPreference.toDomain(): SearchFilterType = when (this) {
    FilterOptionPreference.All -> SearchFilterType.All
    FilterOptionPreference.Login -> SearchFilterType.Login
    FilterOptionPreference.Alias -> SearchFilterType.Alias
    FilterOptionPreference.Note -> SearchFilterType.Note
    FilterOptionPreference.CreditCard -> SearchFilterType.CreditCard
    FilterOptionPreference.Identity -> SearchFilterType.Identity
    FilterOptionPreference.LoginMFA -> SearchFilterType.LoginMFA
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

internal fun FilterOption.toPreference(): FilterOptionPreference = when (this.searchFilterType) {
    SearchFilterType.All -> FilterOptionPreference.All
    SearchFilterType.Login -> FilterOptionPreference.Login
    SearchFilterType.Alias -> FilterOptionPreference.Alias
    SearchFilterType.Note -> FilterOptionPreference.Note
    SearchFilterType.CreditCard -> FilterOptionPreference.CreditCard
    SearchFilterType.Identity -> FilterOptionPreference.Identity
    SearchFilterType.LoginMFA -> FilterOptionPreference.LoginMFA
}

internal fun VaultSelectionOption.toPreference(): SelectedVaultPreference = when (this) {
    VaultSelectionOption.AllVaults -> SelectedVaultPreference.AllVaults
    is VaultSelectionOption.Vault -> SelectedVaultPreference.Vault(this.shareId.id)
    VaultSelectionOption.Trash -> SelectedVaultPreference.Trash
}

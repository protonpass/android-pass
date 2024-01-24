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

package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import proton.android.pass.common.api.Option
import proton.android.pass.data.api.usecases.GetSuggestedLoginItems
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveActiveItems
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.android.pass.data.impl.autofill.SuggestionItemFilterer
import proton.android.pass.data.impl.autofill.SuggestionSorter
import proton.android.pass.domain.Item
import proton.android.pass.domain.PlanType
import proton.android.pass.domain.ShareSelection
import proton.android.pass.domain.canCreate
import proton.android.pass.domain.toPermissions
import javax.inject.Inject

class GetSuggestedLoginItemsImpl @Inject constructor(
    private val getUserPlan: GetUserPlan,
    private val observeActiveItems: ObserveActiveItems,
    private val suggestionItemFilter: SuggestionItemFilterer,
    private val suggestionSorter: SuggestionSorter,
    private val observeVaults: ObserveVaults
) : GetSuggestedLoginItems {

    override fun invoke(
        packageName: Option<String>,
        url: Option<String>
    ): Flow<List<Item>> = getUserPlan().flatMapLatest { userPlan ->
        when (userPlan.planType) {
            is PlanType.Paid,
            is PlanType.Trial -> observeActiveItems(filter = ItemTypeFilter.Logins)

            is PlanType.Free,
            is PlanType.Unknown -> observeActiveItemsForWriteableVaults()
        }
            .map { items -> suggestionItemFilter.filter(items, packageName, url) }
            .map { suggestions -> suggestionSorter.sort(suggestions, url) }
    }

    private fun observeActiveItemsForWriteableVaults(): Flow<List<Item>> = observeVaults()
        .flatMapLatest { vaults ->
            val writeableVaults = vaults
                .filter { it.role.toPermissions().canCreate() }
                .map { it.shareId }
            observeActiveItems(
                filter = ItemTypeFilter.Logins,
                shareSelection = ShareSelection.Shares(writeableVaults)
            )
        }

}

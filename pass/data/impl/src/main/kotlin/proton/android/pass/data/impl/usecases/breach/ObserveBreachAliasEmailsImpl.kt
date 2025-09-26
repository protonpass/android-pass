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

package proton.android.pass.data.impl.usecases.breach

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.runningFold
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveItems
import proton.android.pass.data.api.usecases.breach.ObserveBreachAliasEmails
import proton.android.pass.data.api.usecases.breach.ObserveBreachesForAliasEmail
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.ShareFlag
import proton.android.pass.domain.ShareSelection
import proton.android.pass.domain.breach.AliasData
import proton.android.pass.domain.breach.AliasKeyId
import javax.inject.Inject

class ObserveBreachAliasEmailsImpl @Inject constructor(
    private val observeItems: ObserveItems,
    private val observeBreachesForAliasEmail: ObserveBreachesForAliasEmail
) : ObserveBreachAliasEmails {

    override fun invoke(): Flow<Map<AliasKeyId, AliasData>> = observeItems(
        selection = ShareSelection.AllShares,
        itemState = ItemState.Active,
        filter = ItemTypeFilter.Aliases,
        shareFlags = mapOf(ShareFlag.IsHidden to false)
    )
        .flatMapLatest { list ->
            list
                .map { item ->
                    val aliasKey = AliasKeyId(
                        shareId = item.shareId,
                        itemId = item.id,
                        alias = (item.itemType as ItemType.Alias).aliasEmail
                    )
                    if (item.hasSkippedHealthCheck) {
                        flowOf(aliasKey to AliasData(emptyList(), false))
                    } else {
                        if (item.isEmailBreached) {
                            observeBreachesForAliasEmail(
                                shareId = aliasKey.shareId,
                                itemId = aliasKey.itemId
                            ).map { list -> aliasKey to AliasData(list, true) }
                        } else {
                            flowOf(aliasKey to AliasData(emptyList(), true))
                        }
                    }
                }
                .asFlow()
        }
        .flatMapMerge { it }
        .runningFold(mapOf()) { acc, map -> acc + map }

}

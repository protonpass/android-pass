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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.datetime.Instant
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.BreachRepository
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.ObserveItems
import proton.android.pass.data.api.usecases.breach.ObserveAllBreachByUserId
import proton.android.pass.domain.ItemFlag
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ShareSelection
import proton.android.pass.domain.breach.AliasEmailId
import proton.android.pass.domain.breach.Breach
import proton.android.pass.domain.breach.BreachAlias
import javax.inject.Inject

class ObserveAllBreachByUserIdImpl @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUser,
    private val repository: BreachRepository,
    private val observeItems: ObserveItems
) : ObserveAllBreachByUserId {

    override fun invoke(): Flow<Breach> = observeCurrentUser()
        .flatMapLatest { user -> observeBreaches(user.userId) }

    private fun observeBreaches(userId: UserId): Flow<Breach> = combine(
        repository.observeAllBreaches(userId),
        observeBreachesForAliases(userId)
    ) { addressBreaches, aliasBreaches ->
        addressBreaches.copy(
            breachedAliases = aliasBreaches,
            breachesCount = addressBreaches.breachesCount + aliasBreaches.size
        )
    }

    private fun observeBreachesForAliases(userId: UserId): Flow<List<BreachAlias>> = observeItems(
        selection = ShareSelection.AllShares,
        filter = ItemTypeFilter.Aliases,
        itemState = ItemState.Active,
        itemFlags = mapOf(ItemFlag.EmailBreached to true, ItemFlag.SkipHealthCheck to false),
        includeHidden = false
    ).flatMapLatest { aliases ->
        val firstAlias = aliases.firstOrNull()
            ?: return@flatMapLatest flowOf(emptyList<BreachAlias>())
        val aliasEmailId = AliasEmailId(
            shareId = firstAlias.shareId,
            itemId = firstAlias.id
        )

        repository.observeBreachesForAliasEmail(userId, aliasEmailId)
            .mapLatest { breachesForAlias ->
                breachesForAlias.map { breachEmail ->
                    val parsedTime = runCatching {
                        Instant.parse(breachEmail.publishedAt)
                    }.getOrElse {
                        Instant.DISTANT_PAST
                    }.epochSeconds
                    BreachAlias(
                        shareId = firstAlias.shareId,
                        itemId = firstAlias.id,
                        email = breachEmail.email,
                        breachCounter = breachesForAlias.size,
                        flags = firstAlias.itemFlags.value,
                        lastBreachTime = parsedTime
                    )
                }
            }
    }
}

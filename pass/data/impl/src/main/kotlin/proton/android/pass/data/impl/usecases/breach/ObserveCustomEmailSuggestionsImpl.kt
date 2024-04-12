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
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.common.api.CommonRegex.EMAIL_VALIDATION_REGEX
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveItems
import proton.android.pass.data.api.usecases.breach.CustomEmailSuggestion
import proton.android.pass.data.api.usecases.breach.ObserveCustomEmailSuggestions
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.ShareSelection
import javax.inject.Inject

class ObserveCustomEmailSuggestionsImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val observeItems: ObserveItems
) : ObserveCustomEmailSuggestions {
    override fun invoke(): Flow<List<CustomEmailSuggestion>> = accountManager.getPrimaryUserId()
        .filterNotNull()
        .flatMapLatest { userId ->
            val itemsFlow = observeItems(
                userId = userId,
                selection = ShareSelection.AllShares,
                filter = ItemTypeFilter.Logins,
                itemState = null
            )

            val aliasesFlow = observeItems(
                userId = userId,
                selection = ShareSelection.AllShares,
                filter = ItemTypeFilter.Aliases,
                itemState = null
            )
            combine(itemsFlow, aliasesFlow) { logins, aliases -> combineItems(logins, aliases) }
        }

    private fun combineItems(loginItems: List<Item>, aliasItems: List<Item>): List<CustomEmailSuggestion> {
        val aliases = aliasItems
            .mapNotNull { it.itemType as? ItemType.Alias }
            .map { it.aliasEmail }
            .toSet()

        val loginUsernames = loginItems
            .mapNotNull { it.itemType as? ItemType.Login }
            .map { it.username }
            .filter { username ->
                !aliases.contains(username) && EMAIL_VALIDATION_REGEX.matches(username)
            }

        val loginUsernamesCount = loginUsernames.groupingBy { it }.eachCount()

        return loginUsernamesCount
            .map { (email, count) -> CustomEmailSuggestion(email, count) }
            .sortedByDescending { it.usedInLoginsCount }
    }
}

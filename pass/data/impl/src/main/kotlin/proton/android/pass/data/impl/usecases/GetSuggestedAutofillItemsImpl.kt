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

package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getAccounts
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.toOption
import proton.android.pass.data.api.ItemFilterProcessor
import proton.android.pass.data.api.repositories.AssetLinkRepository
import proton.android.pass.data.api.usecases.GetSuggestedAutofillItems
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.ItemData
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveItems
import proton.android.pass.data.api.usecases.SuggestedAutofillItemsResult
import proton.android.pass.data.api.usecases.Suggestion
import proton.android.pass.data.api.usecases.shares.ObserveAutofillShares
import proton.android.pass.data.impl.autofill.SuggestionItemFilterer
import proton.android.pass.data.impl.autofill.SuggestionSorter
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.Plan
import proton.android.pass.domain.Share
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareSelection
import proton.android.pass.preferences.InternalSettingsRepository
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import javax.inject.Inject

class GetSuggestedAutofillItemsImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val observeItems: ObserveItems,
    private val suggestionItemFilter: SuggestionItemFilterer,
    private val suggestionSorter: SuggestionSorter,
    private val observeAutofillShares: ObserveAutofillShares,
    private val getUserPlan: GetUserPlan,
    private val internalSettingsRepository: InternalSettingsRepository,
    private val assetLinkRepository: AssetLinkRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : GetSuggestedAutofillItems {

    override fun invoke(
        itemTypeFilter: ItemTypeFilter,
        suggestion: Suggestion,
        userId: Option<UserId>
    ): Flow<SuggestedAutofillItemsResult> = getUserIds(userId)
        .flatMapLatest { userIds ->
            when (itemTypeFilter) {
                ItemTypeFilter.Logins,
                ItemTypeFilter.Identity ->
                    handleAllowedItems(userIds, itemTypeFilter, suggestion)

                ItemTypeFilter.CreditCards ->
                    handleCreditCards(userIds, itemTypeFilter, suggestion)

                ItemTypeFilter.All,
                ItemTypeFilter.Aliases,
                ItemTypeFilter.Custom,
                ItemTypeFilter.Notes -> throw IllegalArgumentException("ItemType is not supported: $itemTypeFilter")
            }
        }

    private fun getUserIds(userId: Option<UserId>): Flow<List<UserId>> = if (userId is Some) {
        flowOf(listOf(userId.value))
    } else {
        accountManager.getAccounts(AccountState.Ready)
            .map { accounts -> accounts.map(Account::userId) }
    }

    private fun handleAllowedItems(
        userIds: List<UserId>,
        itemTypeFilter: ItemTypeFilter,
        suggestion: Suggestion
    ): Flow<SuggestedAutofillItemsResult> {
        val accountFlows = userIds.map { userId ->
            getSuggestedItemsForAccount(userId, itemTypeFilter, suggestion)
        }
        return combine(accountFlows, ::processAllowedItemsFlows)
    }

    private fun handleCreditCards(
        userIds: List<UserId>,
        itemTypeFilter: ItemTypeFilter,
        suggestion: Suggestion
    ): Flow<SuggestedAutofillItemsResult> {
        val accountFlows = userIds.map { userId ->
            combine(
                getSuggestedItemsForAccount(userId, itemTypeFilter, suggestion),
                getUserPlan(userId)
            ) { (vaults, items), plan -> Triple(vaults, items, plan) }
        }

        return combine(accountFlows, ::processCreditCardFlows)
    }

    private suspend fun processAllowedItemsFlows(
        array: Array<Pair<List<Share>, List<ItemData.SuggestedItem>>>
    ): SuggestedAutofillItemsResult {
        val filteredItems = ItemFilterProcessor.removeDuplicates(array)
        val sortedItems = sortSuggestions(filteredItems)
        return SuggestedAutofillItemsResult.Items(sortedItems)
    }

    private suspend fun processCreditCardFlows(
        array: Array<Triple<List<Share>, List<ItemData.SuggestedItem>, Plan>>
    ): SuggestedAutofillItemsResult {
        val filteredItems =
            ItemFilterProcessor.removeDuplicates(array.map { it.first to it.second }.toTypedArray())
        val sortedItems = sortSuggestions(filteredItems)
        val plans = array.map { it.third }
        return when {
            plans.all { it.isFreePlan } && sortedItems.isEmpty() ->
                SuggestedAutofillItemsResult.Items(emptyList())

            plans.all { it.isFreePlan } -> SuggestedAutofillItemsResult.ShowUpgrade
            plans.any { it.hasPlanWithAccess } -> {
                if (plans.any { it.isFreePlan }) {
                    val freePlanIndex = array.indexOfFirst { it.third.isFreePlan }
                    val sharesToRemove = array[freePlanIndex].first
                    val filteredItemsWithAccess = sortedItems.filter { suggestedItem ->
                        sharesToRemove.none { it.id == suggestedItem.item.shareId }
                    }
                    SuggestedAutofillItemsResult.Items(filteredItemsWithAccess)
                } else {
                    SuggestedAutofillItemsResult.Items(sortedItems)
                }
            }

            else -> SuggestedAutofillItemsResult.Items(emptyList())
        }
    }

    private fun getSuggestedItemsForAccount(
        userId: UserId,
        itemTypeFilter: ItemTypeFilter,
        suggestion: Suggestion
    ): Flow<Pair<List<Share>, List<ItemData.SuggestedItem>>> = observeAutofillShares(userId)
        .flatMapLatest { autofillShares ->
            combine(
                observeItems(
                    userId = userId,
                    filter = itemTypeFilter,
                    selection = ShareSelection.Shares(autofillShares.map(Share::id)),
                    itemState = ItemState.Active,
                    includeHidden = false
                ),
                getUrlFromPackageNameFlow(suggestion),
                userPreferencesRepository.observeUseDigitalAssetLinksPreference().map { it.value() }
            ) { items, digitalAssetLinkSuggestions, isDALEnabled ->
                val filteredItems = suggestionItemFilter.filter(items, suggestion)
                    .map { item -> ItemData.SuggestedItem(item, suggestion) }
                val combinedItems: List<ItemData.SuggestedItem> = if (isDALEnabled) {
                    filteredItems + digitalAssetLinkSuggestions.flatMap {
                        suggestionItemFilter.filter(items, it)
                            .map { item -> ItemData.SuggestedItem(item, it) }
                    }
                } else {
                    filteredItems
                }

                val uniqueItems = mutableSetOf<Pair<ShareId, ItemId>>()
                val deduplicatedItems = combinedItems.filter { item ->
                    val pair = item.item.shareId to item.item.id
                    uniqueItems.add(pair)
                }
                autofillShares to deduplicatedItems
            }
        }

    private fun getUrlFromPackageNameFlow(suggestion: Suggestion): Flow<List<Suggestion.Url>> = when (suggestion) {
        is Suggestion.PackageName -> assetLinkRepository.observeByPackageName(suggestion.value)
        is Suggestion.Url -> flowOf(emptyList())
    }.map { list -> list.map { Suggestion.Url(it.website, true) } }

    private suspend fun sortSuggestions(items: List<ItemData.SuggestedItem>): List<ItemData.SuggestedItem> {
        val lastAutofillItem =
            internalSettingsRepository.getLastItemAutofill().firstOrNull().toOption().flatMap { it }
        return suggestionSorter.sort(items, lastAutofillItem)
    }
}

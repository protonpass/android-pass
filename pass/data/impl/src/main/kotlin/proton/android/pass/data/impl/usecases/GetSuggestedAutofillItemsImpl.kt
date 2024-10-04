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
import proton.android.pass.data.api.usecases.GetSuggestedAutofillItems
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveItems
import proton.android.pass.data.api.usecases.ObserveUsableVaults
import proton.android.pass.data.api.usecases.SuggestedAutofillItemsResult
import proton.android.pass.data.api.usecases.Suggestion
import proton.android.pass.data.impl.autofill.SuggestionItemFilterer
import proton.android.pass.data.impl.autofill.SuggestionSorter
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.Plan
import proton.android.pass.domain.ShareSelection
import proton.android.pass.domain.Vault
import proton.android.pass.preferences.InternalSettingsRepository
import javax.inject.Inject

class GetSuggestedAutofillItemsImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val observeItems: ObserveItems,
    private val suggestionItemFilter: SuggestionItemFilterer,
    private val suggestionSorter: SuggestionSorter,
    private val observeUsableVaults: ObserveUsableVaults,
    private val getUserPlan: GetUserPlan,
    private val internalSettingsRepository: InternalSettingsRepository
) : GetSuggestedAutofillItems {

    override fun invoke(
        itemTypeFilter: ItemTypeFilter,
        suggestion: Option<Suggestion>,
        userId: Option<UserId>
    ): Flow<SuggestedAutofillItemsResult> = getUserIds(userId)
        .flatMapLatest { userIds ->
            when (itemTypeFilter) {
                ItemTypeFilter.Logins,
                ItemTypeFilter.Identity ->
                    handleAllowedItems(userIds, itemTypeFilter, suggestion)

                ItemTypeFilter.CreditCards ->
                    handleCreditCards(userIds, itemTypeFilter, suggestion)

                else -> throw IllegalArgumentException("ItemType is not supported: $itemTypeFilter")
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
        suggestion: Option<Suggestion>
    ): Flow<SuggestedAutofillItemsResult> {
        val accountFlows = userIds.map { userId ->
            getSuggestedItemsForAccount(userId, itemTypeFilter, suggestion)
        }
        return combine(accountFlows) { array -> processAllowedItemsFlows(array, suggestion) }
    }

    private fun handleCreditCards(
        userIds: List<UserId>,
        itemTypeFilter: ItemTypeFilter,
        suggestion: Option<Suggestion>
    ): Flow<SuggestedAutofillItemsResult> {
        val accountFlows = userIds.map { userId ->
            combine(
                getSuggestedItemsForAccount(userId, itemTypeFilter, suggestion),
                getUserPlan(userId)
            ) { (vaults, items), plan -> Triple(vaults, items, plan) }
        }

        return combine(accountFlows) { array ->
            processCreditCardFlows(array, suggestion)
        }
    }

    private suspend fun processAllowedItemsFlows(
        array: Array<Pair<List<Vault>, List<Item>>>,
        suggestion: Option<Suggestion>
    ): SuggestedAutofillItemsResult {
        val filteredItems = ItemFilterProcessor.removeDuplicates(array)
        val sortedItems = sortSuggestions(filteredItems, suggestion)
        return SuggestedAutofillItemsResult.Items(sortedItems)
    }

    private suspend fun processCreditCardFlows(
        array: Array<Triple<List<Vault>, List<Item>, Plan>>,
        suggestion: Option<Suggestion>
    ): SuggestedAutofillItemsResult {
        val filteredItems =
            ItemFilterProcessor.removeDuplicates(array.map { it.first to it.second }.toTypedArray())
        val sortedItems = sortSuggestions(filteredItems, suggestion)
        val plans = array.map { it.third }
        return when {
            plans.all { it.isFreePlan } && sortedItems.isEmpty() ->
                SuggestedAutofillItemsResult.Items(emptyList())

            plans.all { it.isFreePlan } -> SuggestedAutofillItemsResult.ShowUpgrade
            plans.any { it.hasPlanWithAccess } -> {
                if (plans.any { it.isFreePlan }) {
                    val freePlanIndex = array.indexOfFirst { it.third.isFreePlan }
                    val vaultsToRemove = array[freePlanIndex].first
                    val filteredItemsWithAccess =
                        sortedItems.filter { item -> vaultsToRemove.none { it.shareId == item.shareId } }
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
        suggestion: Option<Suggestion>
    ): Flow<Pair<List<Vault>, List<Item>>> = observeUsableVaults(userId)
        .flatMapLatest { usableVaults ->
            observeItems(
                userId = userId,
                filter = itemTypeFilter,
                selection = ShareSelection.Shares(usableVaults.map(Vault::shareId)),
                itemState = ItemState.Active
            ).map { items ->
                usableVaults to suggestionItemFilter.filter(items, suggestion)
            }
        }

    private suspend fun sortSuggestions(items: List<Item>, suggestion: Option<Suggestion>): List<Item> {
        val lastAutofillItem =
            internalSettingsRepository.getLastItemAutofill().firstOrNull().toOption().flatMap { it }
        return suggestionSorter.sort(items, suggestion.map(Suggestion::value), lastAutofillItem)
    }
}

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
import proton.android.pass.data.api.usecases.GetSuggestedAutofillItems
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveItems
import proton.android.pass.data.api.usecases.ObserveUsableVaults
import proton.android.pass.data.api.usecases.SuggestedAutofillItemsResult
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
        packageName: Option<String>,
        url: Option<String>,
        userId: Option<UserId>
    ): Flow<SuggestedAutofillItemsResult> = getUserIds(userId)
        .flatMapLatest { userIds ->
            when (itemTypeFilter) {
                ItemTypeFilter.Logins,
                ItemTypeFilter.Identity ->
                    handleAllowedItems(userIds, itemTypeFilter, packageName, url)

                ItemTypeFilter.CreditCards ->
                    handleCreditCards(userIds, itemTypeFilter, packageName, url)

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
        packageName: Option<String>,
        url: Option<String>
    ): Flow<SuggestedAutofillItemsResult> {
        val accountFlows = userIds.map { userId ->
            getSuggestedItemsForAccount(userId, itemTypeFilter, packageName, url)
        }
        return combine(accountFlows) { array -> processAccountFlows(array, url) }
    }

    private fun handleCreditCards(
        userIds: List<UserId>,
        itemTypeFilter: ItemTypeFilter,
        packageName: Option<String>,
        url: Option<String>
    ): Flow<SuggestedAutofillItemsResult> {
        val accountFlows = userIds.map { userId ->
            combine(
                getSuggestedItemsForAccount(userId, itemTypeFilter, packageName, url),
                getUserPlan(userId)
            ) { (vaults, items), plan -> Triple(vaults, items, plan) }
        }

        return combine(accountFlows) { array ->
            processCreditCardFlows(array, url)
        }
    }

    private suspend fun processAccountFlows(
        array: Array<Pair<List<Vault>, List<Item>>>,
        url: Option<String>
    ): SuggestedAutofillItemsResult {
        val distinctVaults = getDistinctVaults(array.map { it.first }.flatten())
        val filteredItems = filterItemsByVaults(array.map { it.second }.flatten(), distinctVaults)
        val sortedItems = sortSuggestions(filteredItems, url)
        return SuggestedAutofillItemsResult.Items(sortedItems)
    }

    private suspend fun processCreditCardFlows(
        array: Array<Triple<List<Vault>, List<Item>, Plan>>,
        url: Option<String>
    ): SuggestedAutofillItemsResult {
        val distinctVaults = getDistinctVaults(array.map { it.first }.flatten())
        val filteredItems = array.map { (_, items, plan) ->
            if (plan.hasPlanWithAccess) items else emptyList()
        }.flatten().filter { item -> distinctVaults.any { it.shareId == item.shareId } }
        val sortedItems = sortSuggestions(filteredItems, url)
        val plans = array.map { it.third }
        return when {
            plans.all { it.isFreePlan } && sortedItems.isEmpty() ->
                SuggestedAutofillItemsResult.Items(emptyList())

            plans.all { it.isFreePlan } -> SuggestedAutofillItemsResult.ShowUpgrade
            plans.any { it.hasPlanWithAccess } -> SuggestedAutofillItemsResult.Items(sortedItems)
            else -> SuggestedAutofillItemsResult.Items(emptyList())
        }
    }

    private fun getDistinctVaults(vaultsList: List<Vault>): List<Vault> = vaultsList.groupBy { it.vaultId }
        .mapValues { (_, vaults) ->
            vaults.sortedWith(compareBy({ !it.isOwned }, Vault::role)).first()
        }
        .values
        .toList()

    private fun filterItemsByVaults(items: List<Item>, vaults: List<Vault>): List<Item> =
        items.filter { item -> vaults.any { it.shareId == item.shareId } }

    private fun getSuggestedItemsForAccount(
        userId: UserId,
        itemTypeFilter: ItemTypeFilter,
        packageName: Option<String>,
        url: Option<String>
    ): Flow<Pair<List<Vault>, List<Item>>> = observeUsableVaults(userId)
        .flatMapLatest { usableVaults ->
            observeItems(
                userId = userId,
                filter = itemTypeFilter,
                selection = ShareSelection.Shares(usableVaults.map(Vault::shareId)),
                itemState = ItemState.Active
            ).map { items ->
                usableVaults to suggestionItemFilter.filter(items, packageName, url)
            }
        }

    private suspend fun sortSuggestions(items: List<Item>, url: Option<String>): List<Item> {
        val lastAutofillItem =
            internalSettingsRepository.getLastItemAutofill().firstOrNull().toOption().flatMap { it }
        return suggestionSorter.sort(items, url, lastAutofillItem)
    }
}


package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import proton.android.pass.common.api.Option
import proton.android.pass.data.api.usecases.GetSuggestedLoginItems
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveActiveItems
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.android.pass.data.impl.autofill.SuggestionItemFilterer
import proton.android.pass.data.impl.autofill.SuggestionSorter
import proton.android.pass.log.api.PassLogger
import proton.pass.domain.Item
import proton.pass.domain.PlanType
import proton.pass.domain.ShareId
import proton.pass.domain.ShareSelection
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
    ): Flow<List<Item>> = getUserPlan()
        .flatMapLatest {
            val flow = when (it.planType) {
                is PlanType.Paid, is PlanType.Trial -> observeActiveItems(filter = ItemTypeFilter.Logins)
                else -> observeActiveItemsForPrimaryVault()
            }
            flow
                .map { items -> suggestionItemFilter.filter(items, packageName, url) }
                .map { suggestions -> suggestionSorter.sort(suggestions, url) }
        }

    private fun observeActiveItemsForPrimaryVault(): Flow<List<Item>> = getPrimaryVault()
        .flatMapLatest {
            it.fold(
                onSuccess = { shareId ->
                    observeActiveItems(
                        filter = ItemTypeFilter.Logins,
                        shareSelection = ShareSelection.Share(shareId)
                    )
                },
                onFailure = {
                    PassLogger.w(TAG, it, "Error getting primary vault")
                    flowOf(emptyList())
                }
            )
        }

    private fun getPrimaryVault(): Flow<Result<ShareId>> = observeVaults()
        .map { vaults ->
            val primary = vaults.firstOrNull { it.isPrimary }
                ?: vaults.firstOrNull()

            if (primary == null) {
                Result.failure(IllegalStateException("No vaults found"))
            } else {
                Result.success(primary.shareId)
            }
        }

    companion object {
        private const val TAG = "GetSuggestedLoginItemsImpl"
    }
}

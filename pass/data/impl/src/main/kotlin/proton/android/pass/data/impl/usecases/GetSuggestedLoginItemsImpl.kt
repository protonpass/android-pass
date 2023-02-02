package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.map
import proton.android.pass.data.api.usecases.GetSuggestedLoginItems
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveActiveItems
import proton.android.pass.data.impl.autofill.SuggestionItemFilterer
import proton.android.pass.data.impl.autofill.SuggestionSorter
import proton.pass.domain.Item
import javax.inject.Inject

class GetSuggestedLoginItemsImpl @Inject constructor(
    private val observeActiveItems: ObserveActiveItems,
    private val suggestionItemFilter: SuggestionItemFilterer,
    private val suggestionSorter: SuggestionSorter
) : GetSuggestedLoginItems {
    override fun invoke(
        packageName: Option<String>,
        url: Option<String>
    ): Flow<LoadingResult<List<Item>>> =
        observeActiveItems(filter = ItemTypeFilter.Logins)
            .map { result ->
                result
                    .map { items -> suggestionItemFilter.filter(items, packageName, url) }
                    .map { suggestions -> suggestionSorter.sort(suggestions, url) }
            }
}

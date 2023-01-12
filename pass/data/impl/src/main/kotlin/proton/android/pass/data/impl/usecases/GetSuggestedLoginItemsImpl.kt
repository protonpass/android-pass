package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Result
import proton.android.pass.common.api.map
import proton.android.pass.data.api.usecases.GetSuggestedLoginItems
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveActiveItems
import proton.android.pass.data.impl.autofill.SuggestionItemFilterer
import proton.android.pass.data.impl.autofill.SuggestionSorter
import proton.pass.domain.Item
import javax.inject.Inject

class GetSuggestedLoginItemsImpl @Inject constructor(
    private val observeActiveItems: ObserveActiveItems
) : GetSuggestedLoginItems {
    override fun invoke(
        packageName: Option<String>,
        url: Option<String>
    ): Flow<Result<List<Item>>> =
        observeActiveItems(filter = ItemTypeFilter.Logins)
            .map { result ->
                result
                    .map { items -> SuggestionItemFilterer.filter(items, packageName, url) }
                    .map { suggestions -> SuggestionSorter.sort(suggestions, url) }
            }
}

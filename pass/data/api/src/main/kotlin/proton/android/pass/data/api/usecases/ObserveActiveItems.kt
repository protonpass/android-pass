package proton.android.pass.data.api.usecases

import kotlinx.coroutines.flow.Flow
import proton.android.pass.common.api.Result
import proton.pass.domain.Item
import proton.pass.domain.ShareSelection

enum class ItemTypeFilter {
    All,
    Logins,
    Aliases,
    Notes;
}

interface ObserveActiveItems {
    operator fun invoke(
        filter: ItemTypeFilter = ItemTypeFilter.All,
        shareSelection: ShareSelection = ShareSelection.AllShares
    ): Flow<Result<List<Item>>>
}

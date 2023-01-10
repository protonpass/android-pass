package proton.android.pass.data.api.usecases

import kotlinx.coroutines.flow.Flow
import proton.android.pass.common.api.Result
import proton.pass.domain.Item

enum class ItemTypeFilter {
    All,
    Logins,
    Aliases,
    Notes;
}

interface ObserveActiveItems {
    operator fun invoke(
        filter: ItemTypeFilter = ItemTypeFilter.All
    ): Flow<Result<List<Item>>>
}

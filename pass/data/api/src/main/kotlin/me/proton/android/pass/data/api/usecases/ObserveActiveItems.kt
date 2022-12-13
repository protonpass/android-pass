package me.proton.android.pass.data.api.usecases

import kotlinx.coroutines.flow.Flow
import me.proton.pass.common.api.Result
import me.proton.pass.domain.Item

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

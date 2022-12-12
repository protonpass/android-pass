package me.proton.android.pass.data.api.usecases

import kotlinx.coroutines.flow.Flow
import me.proton.pass.common.api.Result
import me.proton.pass.domain.Item

sealed interface ItemTypeFilter {
    object All : ItemTypeFilter
    object Logins : ItemTypeFilter
    object Aliases : ItemTypeFilter
    object Notes : ItemTypeFilter
}

interface ObserveActiveItems {
    operator fun invoke(
        filter: ItemTypeFilter = ItemTypeFilter.All
    ): Flow<Result<List<Item>>>
}

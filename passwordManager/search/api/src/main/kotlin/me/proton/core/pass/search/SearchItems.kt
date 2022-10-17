package me.proton.core.pass.search

import kotlinx.coroutines.flow.Flow
import me.proton.core.pass.common.api.Result
import me.proton.core.pass.domain.Item

interface SearchItems {
    fun observeResults(): Flow<Result<List<Item>>>
    fun updateQuery(query: String)
    fun clearSearch()
}

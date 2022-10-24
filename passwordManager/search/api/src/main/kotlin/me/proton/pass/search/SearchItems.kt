package me.proton.pass.search

import kotlinx.coroutines.flow.Flow
import me.proton.pass.common.api.Result
import me.proton.pass.domain.Item

interface SearchItems {
    fun observeResults(): Flow<Result<List<Item>>>
    fun updateQuery(query: String)
    fun clearSearch()
}

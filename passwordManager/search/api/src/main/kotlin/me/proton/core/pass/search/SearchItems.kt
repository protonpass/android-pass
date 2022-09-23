package me.proton.core.pass.search

import kotlinx.coroutines.flow.Flow
import me.proton.core.pass.domain.Item

interface SearchItems {
    fun observeResults(): Flow<List<Item>>
    fun updateQuery(query: String)
    fun clearSearch()
}

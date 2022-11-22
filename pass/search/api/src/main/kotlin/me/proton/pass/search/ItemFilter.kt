package me.proton.pass.search

import me.proton.pass.common.api.Result
import me.proton.pass.domain.Item

interface ItemFilter {
    fun filterByQuery(itemsResult: Result<List<Item>>, query: String): Result<List<Item>>
}

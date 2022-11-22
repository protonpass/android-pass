package me.proton.android.pass.search.fakes

import me.proton.pass.common.api.Result
import me.proton.pass.common.api.Result.Loading
import me.proton.pass.domain.Item
import me.proton.pass.search.ItemFilter

class TestItemFilter : ItemFilter {
    override fun filterByQuery(
        itemsResult: Result<List<Item>>,
        query: String
    ): Result<List<Item>> = Loading
}

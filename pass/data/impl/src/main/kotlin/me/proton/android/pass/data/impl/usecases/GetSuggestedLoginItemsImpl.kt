package me.proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.proton.android.pass.data.api.usecases.GetSuggestedLoginItems
import me.proton.android.pass.data.api.usecases.ItemTypeFilter
import me.proton.android.pass.data.api.usecases.ObserveActiveItems
import me.proton.pass.common.api.Option
import me.proton.pass.common.api.Result
import me.proton.pass.common.api.Some
import me.proton.pass.common.api.map
import me.proton.pass.domain.Item
import me.proton.pass.domain.ItemType
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
                result.map { list ->
                    list.filter {
                        if (it.itemType is ItemType.Login) {
                            isMatch(packageName, url, it, it.itemType as ItemType.Login)
                        } else {
                            false
                        }
                    }
                }
            }

    private fun isMatch(
        packageName: Option<String>,
        url: Option<String>,
        item: Item,
        login: ItemType.Login
    ): Boolean {
        return packageName is Some && item.allowedPackageNames.contains(packageName.value) ||
            url is Some && login.websites.contains(url.value)
    }
}


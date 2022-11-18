package me.proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.proton.android.pass.data.api.usecases.GetSuggestedLoginItems
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
        observeActiveItems()
            .map { result ->
                result.map { list ->
                    list.filter {
                        if (it.itemType is ItemType.Login) {
                            val itemTypeLogin = it.itemType as ItemType.Login
                            packageName is Some && it.allowedPackageNames.contains(packageName.value) ||
                                url is Some && itemTypeLogin.websites.contains(url.value)
                        } else {
                            false
                        }
                    }
                }
            }
}


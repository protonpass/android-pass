package me.proton.pass.domain.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.proton.pass.common.api.Result
import me.proton.pass.common.api.map
import me.proton.pass.domain.Item
import me.proton.pass.domain.ItemType
import javax.inject.Inject

@JvmInline
value class UrlOrPackage(val value: String)

interface GetSuggestedLoginItems {
    operator fun invoke(urlOrPackage: UrlOrPackage): Flow<Result<List<Item>>>
}

class GetSuggestedLoginItemsImpl @Inject constructor(
    private val observeActiveItems: ObserveActiveItems
) : GetSuggestedLoginItems {
    override fun invoke(urlOrPackage: UrlOrPackage): Flow<Result<List<Item>>> = observeActiveItems()
        .map { result ->
            result.map { list ->
                list.filter {
                    if (it.itemType is ItemType.Login) {
                        if (it.itemType.allowedPackageNames.contains(urlOrPackage.value))
                            return@filter true
                        if (it.itemType.websites.contains(urlOrPackage.value))
                            return@filter true
                        false
                    } else {
                        false
                    }
                }
            }
        }
}

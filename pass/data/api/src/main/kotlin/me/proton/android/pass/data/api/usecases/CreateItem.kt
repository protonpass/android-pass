package me.proton.android.pass.data.api.usecases

import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.domain.Item
import me.proton.pass.domain.ItemContents
import me.proton.pass.domain.ShareId
import me.proton.pass.domain.entity.PackageName

interface CreateItem {
    suspend operator fun invoke(
        userId: UserId,
        shareId: ShareId,
        itemContents: ItemContents,
        packageName: PackageName? = null
    ): Result<Item>
}

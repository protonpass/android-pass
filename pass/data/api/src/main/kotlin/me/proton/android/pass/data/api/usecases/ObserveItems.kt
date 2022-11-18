package me.proton.android.pass.data.api.usecases

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.domain.Item
import me.proton.pass.domain.ItemState
import me.proton.pass.domain.ShareSelection

interface ObserveItems {
    operator fun invoke(userId: UserId, selection: ShareSelection, itemState: ItemState): Flow<Result<List<Item>>>
}

/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.data.fakes.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.FlowUtils.testFlow
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveItems
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemFlag
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ShareSelection
import proton.android.pass.test.domain.ItemTestFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeObserveItems @Inject constructor() : ObserveItems {

    private val fallback: MutableSharedFlow<Result<List<Item>>> = testFlow()
    private val flowsMap = mutableMapOf<Params, MutableSharedFlow<List<Item>>>()

    fun emitValue(value: List<Item>) {
        fallback.tryEmit(Result.success(value))
    }

    fun emit(params: Params, value: List<Item>) {
        flowsMap[params] = flowsMap[params] ?: testFlow()
        flowsMap[params]?.tryEmit(value)
    }

    fun sendException(exception: Exception) {
        fallback.tryEmit(Result.failure(exception))
    }

    override fun invoke(
        selection: ShareSelection,
        itemState: ItemState?,
        filter: ItemTypeFilter,
        userId: UserId?,
        itemFlags: Map<ItemFlag, Boolean>,
        includeHidden: Boolean
    ): Flow<List<Item>> {
        val params = Params(
            selection = selection,
            itemState = itemState,
            filter = filter,
            userId = userId,
            itemFlags = itemFlags
        )
        val flow = flowsMap[params]
        return flow ?: fallback.map { it.getOrThrow() }
    }

    data class DefaultValues(
        val login: Item,
        val alias: Item,
        val note: Item
    ) {
        fun asList(): List<Item> = listOf(login, alias, note)
    }

    companion object {

        val defaultValues = DefaultValues(
            ItemTestFactory.createLogin(itemId = ItemId("login")),
            ItemTestFactory.createAlias(itemId = ItemId("alias")),
            ItemTestFactory.createNote(itemId = ItemId("note"))
        )
    }

    data class Params(
        val selection: ShareSelection = ShareSelection.AllShares,
        val itemState: ItemState? = ItemState.Active,
        val filter: ItemTypeFilter = ItemTypeFilter.All,
        val userId: UserId? = null,
        val itemFlags: Map<ItemFlag, Boolean> = emptyMap()
    )
}

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
import kotlinx.coroutines.flow.map
import proton.android.pass.common.api.FlowUtils.testFlow
import proton.android.pass.data.api.usecases.ObserveItemById
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeObserveItemById @Inject constructor() : ObserveItemById {

    private var result = testFlow<Result<Item>>()
    private val memory: MutableList<Payload> = mutableListOf()

    fun memory(): List<Payload> = memory

    fun emitValue(value: Result<Item>) {
        result.tryEmit(value)
    }

    override fun invoke(shareId: ShareId, itemId: ItemId): Flow<Item> {
        memory.add(Payload(shareId, itemId))
        return result.map { it.getOrThrow() }
    }

    data class Payload(val shareId: ShareId, val itemId: ItemId)
}

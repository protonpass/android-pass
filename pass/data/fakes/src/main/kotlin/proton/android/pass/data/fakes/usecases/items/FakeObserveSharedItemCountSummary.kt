/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.data.fakes.usecases.items

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import proton.android.pass.common.api.FlowUtils.testFlow
import proton.android.pass.data.api.ItemCountSummary
import proton.android.pass.data.api.usecases.items.ObserveSharedItemCountSummary
import proton.android.pass.domain.items.ItemSharedType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeObserveSharedItemCountSummary @Inject constructor() : ObserveSharedItemCountSummary {

    private val sharedItemCountSummaryFlow = testFlow<Result<ItemCountSummary>>()

    fun emit(value: ItemCountSummary) {
        sharedItemCountSummaryFlow.tryEmit(Result.success(value))
    }

    override fun invoke(itemSharedType: ItemSharedType): Flow<ItemCountSummary> =
        sharedItemCountSummaryFlow.map(Result<ItemCountSummary>::getOrThrow)

}

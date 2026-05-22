/*
 * Copyright (c) 2026 Proton AG
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

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.FlowUtils.testFlow
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveEncryptedItemsPaging
import proton.android.pass.domain.ItemEncrypted
import proton.android.pass.domain.ItemFlag
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ShareSelection
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeObserveEncryptedItemsPaging @Inject constructor() : ObserveEncryptedItemsPaging {

    private val pagingDataFlow: MutableSharedFlow<PagingData<ItemEncrypted>> = testFlow()

    fun emitValue(value: PagingData<ItemEncrypted>) {
        pagingDataFlow.tryEmit(value)
    }

    override fun invoke(
        selection: ShareSelection,
        itemState: ItemState?,
        filter: ItemTypeFilter,
        userId: UserId?,
        itemFlags: Map<ItemFlag, Boolean>,
        includeHidden: Boolean
    ): Flow<PagingData<ItemEncrypted>> = pagingDataFlow
}

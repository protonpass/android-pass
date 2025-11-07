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
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.FlowUtils.testFlow
import proton.android.pass.data.api.usecases.ObserveAllShares
import proton.android.pass.domain.Share
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestObserveAllShares @Inject constructor() : ObserveAllShares {

    private val observeAllSharesFlow = testFlow<Result<List<Share>>>()

    override fun invoke(userId: UserId?, includeHidden: Boolean): Flow<List<Share>> = observeAllSharesFlow
        .map { it.getOrThrow() }

    fun sendResult(result: Result<List<Share>>) = observeAllSharesFlow.tryEmit(result)
}

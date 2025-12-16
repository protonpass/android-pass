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

package proton.android.pass.data.impl.usecases.shares

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.ObserveAllShares
import proton.android.pass.data.api.usecases.shares.ObserveAutofillShares
import proton.android.pass.domain.Share
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class ObserveAutofillSharesImpl @Inject constructor(
    private val observeAllShares: ObserveAllShares
) : ObserveAutofillShares {

    override fun invoke(userId: UserId?): Flow<List<Share>> = observeAllShares(userId, includeHidden = false)
        .mapLatest { shares ->
            val (kept, dropped) = shares.partition(Share::canAutofill)

            if (dropped.isNotEmpty()) {
                val droppedIds = dropped.joinToString { it.id.id }
                PassLogger.i(
                    TAG,
                    "Autofill dropped ${dropped.size}/${shares.size} ids=[$droppedIds]"
                )
            }

            kept
        }

    companion object {
        private const val TAG = "ObserveAutofillSharesImpl"
    }
}

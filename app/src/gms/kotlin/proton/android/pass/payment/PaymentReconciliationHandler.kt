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

package proton.android.pass.payment

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.log.api.PassLogger
import proton.android.pass.payments.api.PaymentSessionState
import proton.android.pass.payments.api.Payments

internal class PaymentReconciliationHandler(
    private val scope: CoroutineScope,
    private val payments: Payments,
    private val getPrimaryUserId: suspend () -> UserId?,
    private val refreshUserAccess: suspend (UserId) -> Unit
) {

    fun start() {
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            payments.observeSessionState()
                .filterIsInstance<PaymentSessionState.Reconciled>()
                .collect {
                    val userId = getPrimaryUserId() ?: return@collect
                    safeRunCatching { refreshUserAccess(userId) }
                        .onFailure { PassLogger.w(TAG, "Could not refresh user access after payment reconciliation") }
                }
        }
    }

    private companion object {
        private const val TAG = "PaymentReconciliationHandler"
    }
}

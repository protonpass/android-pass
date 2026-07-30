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

import android.content.Context
import androidx.lifecycle.coroutineScope
import androidx.startup.Initializer
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.firstOrNull
import me.proton.android.payment.Payments
import me.proton.android.payment.capability.HttpCapability
import me.proton.android.payment.capability.StoreCapability
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.commonui.api.PassAppLifecycleProvider
import proton.android.pass.data.api.usecases.RefreshUserAccess
import proton.android.pass.initializer.WorkManagerInitializer
import proton.android.pass.payments.api.Payments as PassPayments

class PaymentsInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            PaymentsInitializerEntryPoint::class.java
        )
        val accountManager = entryPoint.accountManager()
        val refreshUserAccess = entryPoint.refreshUserAccess()
        PaymentsInitializerDelegate(
            startPaymentReconciliationHandler = {
                PaymentReconciliationHandler(
                    scope = entryPoint.passAppLifecycleProvider().lifecycle.coroutineScope,
                    payments = entryPoint.payments(),
                    getPrimaryUserId = { accountManager.getPrimaryUserId().firstOrNull() },
                    refreshUserAccess = { userId -> refreshUserAccess(userId) }
                ).start()
            }
        ).initialize(
            context,
            entryPoint.httpCapability(),
            entryPoint.storeCapability()
        )
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = listOf(WorkManagerInitializer::class.java)

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface PaymentsInitializerEntryPoint {
        fun accountManager(): AccountManager

        fun httpCapability(): HttpCapabilityImpl

        fun passAppLifecycleProvider(): PassAppLifecycleProvider

        fun payments(): PassPayments

        fun refreshUserAccess(): RefreshUserAccess

        fun storeCapability(): StoreCapabilityImpl
    }
}

internal class PaymentsInitializerDelegate(
    private val initializePayments: (Context, HttpCapability, StoreCapability) -> Unit = { context, http, store ->
        Payments.registerHttp(http).registerStore(store).init(context)
    },
    private val startPaymentReconciliationHandler: () -> Unit = {}
) {
    fun initialize(context: Context, http: HttpCapability, store: StoreCapability) {
        initializePayments(context.applicationContext, http, store)
        startPaymentReconciliationHandler()
    }
}

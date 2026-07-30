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
import com.google.common.truth.Truth.assertThat
import me.proton.android.payment.capability.HttpCapability
import me.proton.android.payment.capability.StoreCapability
import proton.android.pass.initializer.WorkManagerInitializer
import proton.android.pass.payment.fakes.FakeHttpCapability
import proton.android.pass.payment.fakes.FakeStoreCapability
import proton.android.pass.test.TestContext
import org.junit.Test

class PaymentsInitializerTest {

    @Test
    fun `depends on WorkManager initialization before resolving payment capabilities`() {
        assertThat(PaymentsInitializer().dependencies()).contains(WorkManagerInitializer::class.java)
    }

    @Test
    fun `forwards application context and capabilities to payment initialization`() {
        val applicationContext = TestContext()
        val context = TestContext(applicationContext)
        var receivedContext: Context? = null
        var receivedHttp: HttpCapability? = null
        var receivedStore: StoreCapability? = null

        PaymentsInitializerDelegate(
            initializePayments = { initializedContext, http, store ->
                receivedContext = initializedContext
                receivedHttp = http
                receivedStore = store
            }
        ).initialize(context, FakeHttpCapability, FakeStoreCapability)

        assertThat(receivedContext).isSameInstanceAs(applicationContext)
        assertThat(receivedHttp).isSameInstanceAs(FakeHttpCapability)
        assertThat(receivedStore).isSameInstanceAs(FakeStoreCapability)
    }

    @Test
    fun `starts reconciliation handling after payment initialization`() {
        val calls = mutableListOf<String>()

        PaymentsInitializerDelegate(
            initializePayments = { _, _, _ -> calls += "initialize payments" },
            startPaymentReconciliationHandler = { calls += "start reconciliation handling" }
        ).initialize(TestContext(), FakeHttpCapability, FakeStoreCapability)

        assertThat(calls).containsExactly(
            "initialize payments",
            "start reconciliation handling"
        ).inOrder()
    }
}

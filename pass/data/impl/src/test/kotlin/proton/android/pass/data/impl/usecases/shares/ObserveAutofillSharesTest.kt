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

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import proton.android.pass.data.api.usecases.shares.ObserveAutofillShares
import proton.android.pass.data.fakes.usecases.FakeObserveAllShares
import proton.android.pass.domain.Share
import proton.android.pass.test.domain.TestShare

internal class ObserveAutofillSharesTest {

    private lateinit var observeAllShares: FakeObserveAllShares

    private lateinit var observeAutofillShares: ObserveAutofillShares

    @Before
    internal fun setup() {
        observeAllShares = FakeObserveAllShares()

        observeAutofillShares = ObserveAutofillSharesImpl(
            observeAllShares = observeAllShares
        )
    }

    @Test
    internal fun `GIVEN no shares WHEN observeAutofillShares THEN no shares are returned`() = runTest {
        val shares = emptyList<Share>()
        observeAllShares.sendResult(Result.success(shares))

        observeAutofillShares().test {
            awaitItem().also { autofillShares ->
                assertThat(autofillShares).isEmpty()
            }
        }
    }

    @Test
    internal fun `GIVEN shares WHEN observeAutofillShares THEN only autofill shares are returned`() = runTest {
        val share1 = TestShare.Vault.create(canAutofill = true)
        val share2 = TestShare.Item.create(canAutofill = true)
        val share3 = TestShare.Vault.create(canAutofill = false)
        val shares = listOf(share1, share2, share3)
        val expectedShares = listOf(share1, share2)
        observeAllShares.sendResult(Result.success(shares))

        observeAutofillShares().test {
            awaitItem().also { autofillShares ->
                assertThat(autofillShares).isEqualTo(expectedShares)
            }
        }
    }

    @Test
    internal fun `GIVEN shares that cannot autofill WHEN observeAutofillShares THEN no shares are returned`() =
        runTest {
            val shares = listOf(
                TestShare.Vault.create(canAutofill = false),
                TestShare.Item.create(canAutofill = false),
                TestShare.Vault.create(canAutofill = false)
            )
            observeAllShares.sendResult(Result.success(shares))

            observeAutofillShares().test {
                awaitItem().also { autofillShares ->
                    assertThat(autofillShares).isEmpty()
                }
            }
        }

}

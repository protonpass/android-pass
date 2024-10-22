/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.data.impl.usecases

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Test
import proton.android.pass.data.fakes.usecases.TestObserveVaults
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import proton.android.pass.test.domain.TestVault

class ObserveUsableVaultsImplTest {

    private lateinit var instance: ObserveUsableVaultsImpl

    private lateinit var observeVaults: TestObserveVaults

    @Before
    fun setup() {
        observeVaults = TestObserveVaults()

        instance = ObserveUsableVaultsImpl(
            observeVaults = observeVaults
        )
    }

    @Test
    fun `can handle empty list`() = runTest {
        setVaults(emptyMap())

        val res = instance().first()
        assertThat(res).isEmpty()
    }

    @Test
    fun `only vaults with canAutofill`() = runTest {
        val vault1 = ShareId("1")
        val vault2 = ShareId("2")
        val vault3 = ShareId("3")

        setVaults(
            mapOf(
                vault1 to true,
                vault2 to true,
                vault3 to false
            )
        )

        val res = instance().first()
        assertThat(res).hasSize(2)
        assertThat(res.map { it.shareId }).containsExactly(vault1, vault2)
    }

    @Test
    fun `can handle all vaults with canAutofill false`() = runTest {
        val vault1 = ShareId("1")
        val vault2 = ShareId("2")
        val vault3 = ShareId("3")

        setVaults(
            mapOf(
                vault1 to false,
                vault2 to false,
                vault3 to false
            )
        )

        val res = instance().first()
        assertThat(res).isEmpty()
    }

    private fun setVaults(vaults: Map<ShareId, Boolean>) {
        val vaultInstances: List<Vault> = vaults.map { (shareId, canAutofill) ->
            TestVault.create(
                shareId = shareId,
                userId = DEFAULT_USER_ID,
                name = "unused",
                canAutofill = canAutofill
            )
        }

        observeVaults.sendResult(Result.success(vaultInstances))
    }

    companion object {
        val DEFAULT_USER_ID = UserId("default-user-id")
    }

}

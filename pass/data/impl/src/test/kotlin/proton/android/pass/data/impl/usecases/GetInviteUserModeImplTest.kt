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

package proton.android.pass.data.impl.usecases

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import proton.android.pass.data.api.usecases.InviteUserMode
import proton.android.pass.data.fakes.usecases.FakeGetAllKeysByAddress
import proton.android.pass.data.impl.network.RemoteApiException
import proton.android.pass.data.impl.network.RemoteResult
import proton.android.pass.domain.UserId

class GetInviteUserModeImplTest {

    private lateinit var getAllKeysByAddress: FakeGetAllKeysByAddress
    private lateinit var instance: GetInviteUserModeImpl

    @Before
    fun setup() {
        getAllKeysByAddress = FakeGetAllKeysByAddress()
        instance = GetInviteUserModeImpl(getAllKeysByAddress)
    }

    @Test
    fun `address does not exist code resolves to a new user`() = runTest {
        getAllKeysByAddress.setResult(Result.failure(http(ADDRESS_NOT_EXIST_CODE)))

        assertThat(invoke().getOrNull()).isEqualTo(InviteUserMode.NewUser)
    }

    @Test
    fun `external address code resolves to a new user`() = runTest {
        getAllKeysByAddress.setResult(Result.failure(http(ADDRESS_EXTERNAL_CODE)))

        assertThat(invoke().getOrNull()).isEqualTo(InviteUserMode.NewUser)
    }

    @Test
    fun `other proton codes are propagated`() = runTest {
        val error = http(OTHER_CODE)
        getAllKeysByAddress.setResult(Result.failure(error))

        assertThat(invoke().exceptionOrNull()).isSameInstanceAs(error)
    }

    @Test
    fun `non http errors are propagated`() = runTest {
        val error = RemoteApiException(RemoteResult.Error.NoInternet())
        getAllKeysByAddress.setResult(Result.failure(error))

        assertThat(invoke().exceptionOrNull()).isSameInstanceAs(error)
    }

    private suspend fun invoke() = instance(UserId("user-id"), "user@example.com")

    private fun http(protonCode: Int) =
        RemoteApiException(RemoteResult.Error.Http(HTTP_UNPROCESSABLE, protonCode, "message"))

    private companion object {
        const val ADDRESS_NOT_EXIST_CODE = 33_102
        const val ADDRESS_EXTERNAL_CODE = 33_103
        const val OTHER_CODE = 2_002
        const val HTTP_UNPROCESSABLE = 422
    }
}

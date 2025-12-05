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

package proton.android.pass.data.impl.repositories

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Test
import org.junit.runner.RunWith
import proton.android.pass.common.fakes.FakeAppDispatchers
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider

@RunWith(AndroidJUnit4::class)
class ExtraPasswordRepositoryTest {

    @Test
    fun shouldEncryptAndDecryptExtraPassword() = runTest {
        val encryptionContextProvider = FakeEncryptionContextProvider()

        val repository = ExtraPasswordRepositoryImpl(
            appDispatchers = FakeAppDispatchers(),
            appContext = InstrumentationRegistry.getInstrumentation().targetContext,
            encryptionContextProvider = encryptionContextProvider
        )

        val password = "password"
        val userId = UserId("123")
        val encryptedPassword = encryptionContextProvider.withEncryptionContext { encrypt(password) }
        repository.storeAccessKeyForUser(userId, encryptedPassword)

        val result = repository.checkAccessKeyForUser(userId, encryptedPassword)
        assert(result)
    }
}

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

package proton.android.pass.uitest.rule

import kotlinx.coroutines.runBlocking
import me.proton.core.auth.domain.testing.LoginTestHelper
import org.junit.rules.ExternalResource
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.extrapassword.SetupExtraPassword

class ExtraPasswordSetupRule(
    private val username: () -> String,
    private val password: () -> String,
    private val extraPassword: String,
    private val loginTestHelper: () -> LoginTestHelper,
    private val setupExtraPassword: () -> SetupExtraPassword,
    private val encryptionContextProvider: () -> EncryptionContextProvider
) : ExternalResource() {

    override fun before() {
        loginTestHelper().login(username(), password())
        runBlocking {
            val encryptedExtraPassword = encryptionContextProvider().withEncryptionContext {
                encrypt(extraPassword)
            }
            setupExtraPassword().invoke(encryptedExtraPassword)
        }
        loginTestHelper().logoutAll()
    }
}

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

package proton.android.pass.data.fakes.usecases.credentials.passkeys

import proton.android.pass.data.api.usecases.credentials.passkeys.GetPasskeyCredentialItems
import proton.android.pass.data.api.usecases.passkeys.PasskeySelection
import proton.android.pass.domain.credentials.PasskeyCredentialItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeGetPasskeyCredentialItems @Inject constructor() : GetPasskeyCredentialItems {

    private var passkeyCredentialItems: List<PasskeyCredentialItem> = emptyList()

    fun setPasskeyCredentialItems(passwordCredentialItems: List<PasskeyCredentialItem>) {
        this.passkeyCredentialItems = passwordCredentialItems
    }

    override suspend fun invoke(domain: String, selection: PasskeySelection): List<PasskeyCredentialItem> =
        passkeyCredentialItems

}

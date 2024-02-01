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

package proton.android.pass.passkeys.api

import proton.android.pass.common.api.Option

data class PasskeySummary(
    val id: String,
    val shareId: String,
    val itemId: String,
    val title: String,
)

interface PasskeyManager {
    suspend fun getPasskeyById(id: String): Option<ByteArray>
    suspend fun storePasskey(content: ByteArray, domain: String, identity: String)
    suspend fun getPasskeysForDomain(domain: String): List<PasskeySummary>
    suspend fun clearPasskeys()
}

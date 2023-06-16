/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.crypto.api.usecases

import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.entity.UserAddress
import proton.android.pass.crypto.api.EncryptionKey
import proton_pass_vault_v1.VaultV1

data class EncryptedCreateVault(
    val addressId: String,
    val content: String,
    val contentFormatVersion: Int,
    val encryptedVaultKey: String,
)

data class CreateVaultOutput(
    val request: EncryptedCreateVault,
    val shareKey: EncryptionKey
)

interface CreateVault {
    fun createVaultRequest(
        user: User,
        userAddress: UserAddress,
        vaultMetadata: VaultV1.Vault
    ): CreateVaultOutput
}

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

package proton.android.pass.crypto.api.extensions

import proton.android.pass.common.api.Option
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.pass.domain.Share
import proton.pass.domain.Vault
import proton_pass_vault_v1.VaultV1

fun Share.toVault(encryptionContextProvider: EncryptionContextProvider): Option<Vault> = content
    .map {
        val decrypted = encryptionContextProvider.withEncryptionContext {
            decrypt(it)
        }
        val parsed = VaultV1.Vault.parseFrom(decrypted)
        Vault(
            shareId = id,
            isPrimary = isPrimary,
            name = parsed.name,
            color = color,
            icon = icon,
            members = memberCount,
            isOwned = isOwner,
            role = shareRole
        )
    }


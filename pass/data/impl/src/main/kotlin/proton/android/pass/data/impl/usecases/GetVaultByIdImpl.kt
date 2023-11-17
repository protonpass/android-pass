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

package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.extensions.toVault
import proton.android.pass.data.api.errors.ShareContentNotAvailableError
import proton.android.pass.data.api.usecases.GetShareById
import proton.android.pass.data.api.usecases.GetVaultById
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import javax.inject.Inject

class GetVaultByIdImpl @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider,
    private val getShareById: GetShareById
) : GetVaultById {

    override fun invoke(userId: UserId?, shareId: ShareId): Flow<Vault> = flow {
        val share = getShareById(userId, shareId)
        when (val asVault = share.toVault(encryptionContextProvider)) {
            None -> throw ShareContentNotAvailableError()
            is Some -> emit(asVault.value)
        }
    }
}

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
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.some
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.extensions.toVault
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.ObserveVaultById
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObserveVaultByIdImpl @Inject constructor(
    private val shareRepository: ShareRepository,
    private val accountManager: AccountManager,
    private val encryptionContextProvider: EncryptionContextProvider
) : ObserveVaultById {
    override fun invoke(userId: UserId?, shareId: ShareId): Flow<Option<Vault>> {
        val userIdFlow = if (userId == null) {
            accountManager.getPrimaryUserId().filterNotNull()
        } else {
            flowOf(userId)
        }
        return userIdFlow.flatMapLatest { id ->
            shareRepository.observeById(id, shareId)
        }.map { share ->
            share.flatMap {
                when (val asVault = it.toVault(encryptionContextProvider)) {
                    None -> None
                    is Some -> asVault.value.some()
                }
            }
        }
    }
}

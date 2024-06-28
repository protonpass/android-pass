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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.toOption
import proton.android.pass.data.api.usecases.GetVaultWithItemCountById
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.android.pass.data.api.usecases.defaultvault.ObserveDefaultVault
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.domain.canCreate
import proton.android.pass.domain.toPermissions
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObserveDefaultVaultImpl @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUser,
    private val preferencesRepository: UserPreferencesRepository,
    private val getVaultWithItemCount: GetVaultWithItemCountById,
    private val observeVaults: ObserveVaults
) : ObserveDefaultVault {

    override fun invoke(): Flow<Option<VaultWithItemCount>> = observeCurrentUser()
        .flatMapLatest { user ->
            preferencesRepository.getDefaultVault(user.userId)
                .map { user.userId to it }
        }
        .flatMapLatest { (userId, defaultVault) ->
            when (defaultVault) {
                None -> {
                    setDefaultVault(userId)
                    flowOf(None)
                }

                is Some -> getVaultWithItemCount(shareId = ShareId(defaultVault.value))
                    .map { it.toOption() }
            }
        }

    private suspend fun setDefaultVault(userId: UserId) {
        val vaults = observeVaults().firstOrNull() ?: run {
            PassLogger.w(TAG, "There are no vaults")
            return
        }

        val defaultVault = vaults.firstOrNull { it.role.toPermissions().canCreate() } ?: run {
            PassLogger.w(TAG, "There are no writable vaults")
            return
        }

        preferencesRepository.setDefaultVault(userId, defaultVault.shareId)
            .onSuccess {
                PassLogger.i(TAG, "Set default vault to ${defaultVault.shareId.id}")
            }
            .onFailure {
                PassLogger.w(TAG, "Error setting default vault to ${defaultVault.shareId.id}")
                PassLogger.w(TAG, it)
            }
    }

    companion object {
        private const val TAG = "ObserveDefaultVaultImpl"
    }
}

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

import kotlinx.coroutines.flow.firstOrNull
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.key.domain.entity.key.PublicAddressKey
import me.proton.core.key.domain.repository.PublicAddressRepository
import me.proton.core.key.domain.repository.getPublicAddressOrNull
import me.proton.core.network.data.ApiProvider
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.data.api.usecases.GetAllKeysByAddress
import proton.android.pass.data.impl.api.PasswordManagerApi
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetAllKeysByAddressImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val publicAddressRepository: PublicAddressRepository,
    private val apiProvider: ApiProvider
) : GetAllKeysByAddress {

    @Suppress("ReturnCount")
    override suspend fun invoke(email: String): Result<List<PublicAddressKey>> {
        val userId = accountManager.getPrimaryUserId().firstOrNull()
            ?: return Result.failure(IllegalStateException("Could not find Primary User Id"))

        // Perform regular lookup
        val remoteAddress = publicAddressRepository.getPublicAddressOrNull(userId, email)
        if (remoteAddress != null && remoteAddress.keys.isNotEmpty()) {
            return Result.success(remoteAddress.keys)
        }

        PassLogger.d(TAG, "Could not find keys for email. Fetching from new endpoint")
        val res = apiProvider.get<PasswordManagerApi>(userId).invoke {
            getAllKeysByAddress(email = email)
        }

        val response = safeRunCatching { res.valueOrThrow }.getOrElse {
            return Result.failure(it)
        }

        if (response.code != CODE_SUCCESS) {
            return Result.failure(IllegalStateException("Could not find keys for email. Code: ${response.code}"))
        }

        val keys = response.address.keys.mapIndexed { idx, key ->
            key.toPublicAddressKey(email = email, isPrimary = idx == 0)
        }

        return Result.success(keys)
    }

    companion object {
        private const val TAG = "GetKeysForEmailImpl"
        private const val CODE_SUCCESS = 1000
    }
}

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
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.repository.PublicAddressRepository
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import proton.android.pass.data.api.usecases.GetInviteUserMode
import proton.android.pass.data.api.usecases.InviteUserMode
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetInviteUserModeImpl @Inject constructor(
    private val ffRepo: FeatureFlagsPreferencesRepository,
    private val publicAddressRepository: PublicAddressRepository
) : GetInviteUserMode {
    override suspend fun invoke(userId: UserId, email: String): Result<InviteUserMode> {
        val isInviteNewUserEnabled = ffRepo.get<Boolean>(FeatureFlag.SHARING_NEW_USERS)
            .firstOrNull()
            ?: false
        return runCatching {
            publicAddressRepository.getPublicAddress(userId, email)
        }.fold(
            onSuccess = { publicAddress ->
                when {
                    publicAddress.keys.isEmpty() -> {
                        PassLogger.i(TAG, "New user invite")
                        if (isInviteNewUserEnabled) {
                            Result.success(InviteUserMode.NewUser)
                        } else {
                            PassLogger.i(TAG, "New user invites are not enabled yet")
                            Result.failure(IllegalStateException("User key list is empty"))
                        }
                    }

                    else -> {
                        PassLogger.i(TAG, "Existing user invite")
                        Result.success(InviteUserMode.ExistingUser)
                    }
                }
            },
            onFailure = {
                return if (it.isAddressNotExistsError() && isInviteNewUserEnabled) {
                    Result.success(InviteUserMode.NewUser)
                } else {
                    Result.failure(it)
                }
            }
        )
    }

    private fun Throwable.isAddressNotExistsError(): Boolean {
        if (this is ApiException) {
            val exceptionError = this.error
            if (exceptionError is ApiResult.Error.Http) {
                val protonData = exceptionError.proton
                if (protonData != null) {
                    if (protonData.code == ERROR_CODE_ADDRESS_NOT_EXIST) {
                        return true
                    }
                }
            }
        }

        return false
    }

    companion object {
        private const val TAG = "GetInviteUserModeImpl"
        private const val ERROR_CODE_ADDRESS_NOT_EXIST = 33_102
    }
}

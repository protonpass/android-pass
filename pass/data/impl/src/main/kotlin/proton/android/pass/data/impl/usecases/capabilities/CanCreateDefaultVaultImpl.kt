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

package proton.android.pass.data.impl.usecases.capabilities

import kotlinx.coroutines.flow.first
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.data.api.usecases.capabilities.CanCreateDefaultVault
import proton.android.pass.data.api.usecases.capabilities.CanCreateVault
import proton.android.pass.data.api.usecases.organization.RefreshOrganizationSettings
import proton.android.pass.domain.UserId
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class CanCreateDefaultVaultImpl @Inject constructor(
    private val refreshOrganizationSettings: RefreshOrganizationSettings,
    private val canCreateVault: CanCreateVault
) : CanCreateDefaultVault {

    override suspend fun invoke(userId: UserId): Boolean {
        safeRunCatching { refreshOrganizationSettings(userId) }
            .onFailure {
                PassLogger.w(TAG, "Could not refresh organization settings before the default vault check")
                PassLogger.w(TAG, it)
            }
        return canCreateVault(userId).first()
    }

    private companion object {
        private const val TAG = "CanCreateDefaultVaultImpl"
    }
}

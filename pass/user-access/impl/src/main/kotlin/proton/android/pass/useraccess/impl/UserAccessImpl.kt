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

package proton.android.pass.useraccess.impl

import kotlinx.coroutines.flow.firstOrNull
import proton.android.pass.data.api.usecases.GetVaultById
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.android.pass.useraccess.api.UserAccess
import proton.pass.domain.ShareId
import javax.inject.Inject

class UserAccessImpl @Inject constructor(
    private val featureFlagsPreferencesRepository: FeatureFlagsPreferencesRepository,
    private val getVaultById: GetVaultById,
) : UserAccess {

    @Suppress("ReturnCount")
    override suspend fun canShare(shareId: ShareId): Boolean {
        val isSharingFlagEnabled: Boolean = featureFlagsPreferencesRepository
            .get<Boolean>(FeatureFlag.SHARING_V1)
            .firstOrNull()
            ?: false
        if (!isSharingFlagEnabled) return false
        val vaultResult = runCatching { getVaultById(shareId = shareId).firstOrNull() }
        val vault = vaultResult.getOrNull() ?: return false
        if (vault.isPrimary) return false
        return true
    }
}

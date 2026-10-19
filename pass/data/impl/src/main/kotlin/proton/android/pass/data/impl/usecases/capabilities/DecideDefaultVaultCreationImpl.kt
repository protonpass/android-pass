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

import kotlinx.coroutines.flow.firstOrNull
import proton.android.pass.data.api.usecases.capabilities.CanCreateDefaultVault
import proton.android.pass.data.api.usecases.capabilities.DecideDefaultVaultCreation
import proton.android.pass.data.api.usecases.capabilities.DefaultVaultDecision
import proton.android.pass.domain.UserId
import proton.android.pass.preferences.InternalSettingsRepository
import javax.inject.Inject

class DecideDefaultVaultCreationImpl @Inject constructor(
    private val canCreateDefaultVault: CanCreateDefaultVault,
    private val internalSettingsRepository: InternalSettingsRepository
) : DecideDefaultVaultCreation {

    override suspend fun invoke(userId: UserId, isForceSync: Boolean): DefaultVaultDecision {
        val hasDefaultVaultBeenCreated = internalSettingsRepository
            .hasDefaultVaultBeenCreated(userId)
            .firstOrNull()
            ?: false

        if (!isForceSync && hasDefaultVaultBeenCreated) return DefaultVaultDecision.SkipAlreadyCreated

        return if (canCreateDefaultVault(userId)) {
            DefaultVaultDecision.Create
        } else {
            DefaultVaultDecision.SkipNotAllowed
        }
    }
}

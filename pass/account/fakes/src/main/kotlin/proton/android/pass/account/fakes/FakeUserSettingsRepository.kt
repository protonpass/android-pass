/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.account.fakes

import kotlinx.coroutines.flow.Flow
import me.proton.core.auth.fido.domain.entity.SecondFactorProof
import me.proton.core.crypto.common.srp.Auth
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.usersettings.domain.entity.UserSettings
import me.proton.core.usersettings.domain.repository.UserSettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeUserSettingsRepository @Inject constructor() : UserSettingsRepository {
    override suspend fun getUserSettings(sessionUserId: SessionUserId, refresh: Boolean): UserSettings {
        throw IllegalStateException("Not implemented")
    }

    override fun getUserSettingsFlow(sessionUserId: SessionUserId, refresh: Boolean): Flow<DataResult<UserSettings>> {
        throw IllegalStateException("Not implemented")
    }

    override fun markAsStale(userId: UserId) {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun setUsername(sessionUserId: SessionUserId, username: String): Boolean {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun updateCrashReports(userId: UserId, isEnabled: Boolean): UserSettings {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun updateLoginPassword(
        sessionUserId: SessionUserId,
        srpProofs: SrpProofs,
        srpSession: String,
        secondFactorProof: SecondFactorProof?,
        auth: Auth
    ): UserSettings {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun updateRecoveryEmail(
        sessionUserId: SessionUserId,
        email: String,
        srpProofs: SrpProofs,
        srpSession: String,
        secondFactorProof: SecondFactorProof?
    ): UserSettings {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun updateTelemetry(userId: UserId, isEnabled: Boolean): UserSettings {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun updateUserSettings(userSettings: UserSettings) {
        throw IllegalStateException("Not implemented")
    }
}

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

package proton.android.pass.data.api.repositories

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import proton.android.pass.domain.breach.AliasEmailId
import proton.android.pass.domain.breach.Breach
import proton.android.pass.domain.breach.BreachCustomEmail
import proton.android.pass.domain.breach.BreachEmail
import proton.android.pass.domain.breach.BreachEmailReport
import proton.android.pass.domain.breach.BreachProtonEmail
import proton.android.pass.domain.breach.CustomEmailId

interface BreachRepository {

    fun observeAllBreaches(userId: UserId): Flow<Breach>

    fun observeCustomEmail(userId: UserId, customEmailId: CustomEmailId): Flow<BreachEmailReport.Custom>

    fun observeCustomEmails(userId: UserId): Flow<List<BreachCustomEmail>>

    suspend fun addCustomEmail(userId: UserId, email: String): BreachCustomEmail

    suspend fun verifyCustomEmail(
        userId: UserId,
        emailId: CustomEmailId,
        code: String
    )

    fun observeAliasEmail(userId: UserId, aliasEmailId: AliasEmailId): Flow<BreachEmailReport.Alias>

    fun observeProtonEmail(userId: UserId, addressId: AddressId): Flow<BreachEmailReport.Proton>

    fun observeProtonEmails(userId: UserId): Flow<List<BreachProtonEmail>>

    fun observeBreachesForProtonEmail(userId: UserId, addressId: AddressId): Flow<List<BreachEmail>>

    fun observeBreachesForCustomEmail(userId: UserId, customEmailId: CustomEmailId): Flow<List<BreachEmail>>

    fun observeBreachesForAliasEmail(userId: UserId, aliasEmailId: AliasEmailId): Flow<List<BreachEmail>>

    suspend fun markProtonEmailAsResolved(userId: UserId, id: AddressId)

    suspend fun markAliasEmailAsResolved(userId: UserId, aliasEmailId: AliasEmailId)

    suspend fun markCustomEmailAsResolved(userId: UserId, id: CustomEmailId)

    suspend fun resendVerificationCode(userId: UserId, id: CustomEmailId)

    suspend fun removeCustomEmail(userId: UserId, id: CustomEmailId)

    suspend fun updateGlobalProtonMonitorState(userId: UserId, enabled: Boolean)

    suspend fun updateGlobalAliasMonitorState(userId: UserId, enabled: Boolean)

    suspend fun updateProtonAddressMonitorState(
        userId: UserId,
        addressId: AddressId,
        enabled: Boolean
    )

    suspend fun refreshBreaches(userId: UserId)
}

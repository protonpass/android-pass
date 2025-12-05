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

package proton.android.pass.data.impl.local

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import proton.android.pass.domain.breach.AliasEmailId
import proton.android.pass.domain.breach.BreachCustomEmail
import proton.android.pass.domain.breach.BreachDomainPeek
import proton.android.pass.domain.breach.BreachEmail
import proton.android.pass.domain.breach.BreachProtonEmail
import proton.android.pass.domain.breach.CustomEmailId

@Suppress("ComplexInterface", "TooManyFunctions")
interface LocalBreachDataSource {

    suspend fun getCustomEmail(userId: UserId, customEmailId: CustomEmailId): BreachCustomEmail

    fun observeCustomEmail(userId: UserId, customEmailId: CustomEmailId): Flow<BreachCustomEmail>

    suspend fun upsertCustomEmail(userId: UserId, customEmail: BreachCustomEmail)

    fun observeCustomEmails(): Flow<List<BreachCustomEmail>>

    fun observeCustomEmails(userId: UserId): Flow<List<BreachCustomEmail>>

    suspend fun upsertCustomEmails(userId: UserId, customEmails: List<BreachCustomEmail>)

    suspend fun deleteCustomEmail(userId: UserId, customEmailId: CustomEmailId)

    fun observeCustomEmailBreaches(userId: UserId, customEmailId: CustomEmailId): Flow<List<BreachEmail>>

    suspend fun upsertCustomEmailBreaches(
        userId: UserId,
        customEmailId: CustomEmailId,
        customEmailBreaches: List<BreachEmail>
    )

    suspend fun getProtonEmail(userId: UserId, addressId: AddressId): BreachProtonEmail

    fun observeProtonEmail(userId: UserId, addressId: AddressId): Flow<BreachProtonEmail>

    suspend fun upsertProtonEmail(userId: UserId, protonEmail: BreachProtonEmail)

    fun observeProtonEmails(userId: UserId): Flow<List<BreachProtonEmail>>

    suspend fun upsertProtonEmails(userId: UserId, protonEmails: List<BreachProtonEmail>)

    fun observeProtonEmailBreaches(userId: UserId, addressId: AddressId): Flow<List<BreachEmail>>

    suspend fun upsertProtonEmailBreaches(
        userId: UserId,
        addressId: AddressId,
        protonEmailBreaches: List<BreachEmail>
    )

    fun observeAliasEmailBreaches(userId: UserId, aliasEmailId: AliasEmailId): Flow<List<BreachEmail>>

    suspend fun upsertAliasEmailBreaches(
        userId: UserId,
        aliasEmailId: AliasEmailId,
        aliasEmailBreaches: List<BreachEmail>
    )

    suspend fun getAliasEmailBreaches(userId: UserId, aliasEmailId: AliasEmailId): List<BreachEmail>

    suspend fun getCustomEmailBreaches(userId: UserId, customEmailId: CustomEmailId): List<BreachEmail>

    suspend fun getProtonEmailBreaches(userId: UserId, id: AddressId): List<BreachEmail>

    fun observeBreachDomainPeeks(userId: UserId): Flow<List<BreachDomainPeek>>

    suspend fun upsertBreachDomainPeeks(userId: UserId, domainPeeks: List<BreachDomainPeek>)

}


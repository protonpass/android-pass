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

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import proton.android.pass.domain.breach.AliasEmailId
import proton.android.pass.domain.breach.BreachCustomEmail
import proton.android.pass.domain.breach.BreachEmail
import proton.android.pass.domain.breach.BreachProtonEmail
import proton.android.pass.domain.breach.CustomEmailId
import javax.inject.Inject

interface LocalBreachesDataSource {

    suspend fun getCustomEmail(userId: UserId, customEmailId: CustomEmailId): BreachCustomEmail

    fun observeCustomEmail(userId: UserId, customEmailId: CustomEmailId): Flow<BreachCustomEmail>

    suspend fun upsertCustomEmail(userId: UserId, customEmail: BreachCustomEmail)

    fun observeCustomEmails(): Flow<List<BreachCustomEmail>>

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

    fun getAliasEmailBreaches(userId: UserId, aliasEmailId: AliasEmailId): List<BreachEmail>

    fun getCustomEmailBreaches(userId: UserId, customEmailId: CustomEmailId): List<BreachEmail>

    fun getProtonEmailBreaches(userId: UserId, id: AddressId): List<BreachEmail>

}

class LocalBreachesDataSourceImpl @Inject constructor() : LocalBreachesDataSource {

    private val customEmailsFlow =
        MutableSharedFlow<Map<Pair<UserId, CustomEmailId>, BreachCustomEmail>>(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )

    private val customEmailBreachesFlow =
        MutableSharedFlow<Map<Pair<UserId, CustomEmailId>, List<BreachEmail>>>(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )

    private val customEmailsCache = mutableMapOf<Pair<UserId, CustomEmailId>, BreachCustomEmail>()

    private val customEmailBreachesCache =
        mutableMapOf<Pair<UserId, CustomEmailId>, List<BreachEmail>>()

    private val protonEmailsFlow =
        MutableSharedFlow<Map<Pair<UserId, AddressId>, BreachProtonEmail>>(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )

    private val protonEmailBreachesFlow =
        MutableSharedFlow<Map<Pair<UserId, AddressId>, List<BreachEmail>>>(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )

    private val protonEmailsCache = mutableMapOf<Pair<UserId, AddressId>, BreachProtonEmail>()

    private val protonEmailBreachesCache =
        mutableMapOf<Pair<UserId, AddressId>, List<BreachEmail>>()

    private val aliasEmailBreachesCache =
        mutableMapOf<Pair<UserId, AliasEmailId>, List<BreachEmail>>()

    private val aliasEmailBreachesFlow =
        MutableSharedFlow<Map<Pair<UserId, AliasEmailId>, List<BreachEmail>>>(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )

    override suspend fun getCustomEmail(userId: UserId, customEmailId: CustomEmailId): BreachCustomEmail =
        customEmailsCache[Pair(userId, customEmailId)]
            ?: throw IllegalArgumentException("There's no custom email with id: ${customEmailId.id}")

    override fun observeCustomEmail(userId: UserId, customEmailId: CustomEmailId): Flow<BreachCustomEmail> =
        customEmailsFlow
            .map { customEmailsMap -> customEmailsMap[Pair(userId, customEmailId)] }
            .filterNotNull()

    override suspend fun upsertCustomEmail(userId: UserId, customEmail: BreachCustomEmail) {
        // Check that it didn't exist previously any entry with the custom email
        for ((key, value) in customEmailsCache) {
            if (value.email == customEmail.email) {
                customEmailsCache.remove(key)
                break
            }
        }

        // Actually add the custom email
        customEmailsCache
            .put(Pair(userId, customEmail.id), customEmail)
            .also { emitCustomEmailsChanges() }
    }

    override fun observeCustomEmails(): Flow<List<BreachCustomEmail>> = customEmailsFlow
        .map { customEmailsMap -> customEmailsMap.values.toList() }

    override suspend fun upsertCustomEmails(userId: UserId, customEmails: List<BreachCustomEmail>) {
        customEmails.toSet()
            .forEach { customEmail ->
                customEmailsCache[Pair(userId, customEmail.id)] = customEmail
            }
            .also { emitCustomEmailsChanges() }
    }

    override suspend fun deleteCustomEmail(userId: UserId, customEmailId: CustomEmailId) {
        customEmailsCache.remove(Pair(userId, customEmailId))
            .also { emitCustomEmailsChanges() }
    }

    override fun observeCustomEmailBreaches(userId: UserId, customEmailId: CustomEmailId): Flow<List<BreachEmail>> =
        customEmailBreachesFlow.map { customEmailBreachesMap ->
            customEmailBreachesMap.getOrElse(Pair(userId, customEmailId)) { emptyList() }
        }

    override suspend fun upsertCustomEmailBreaches(
        userId: UserId,
        customEmailId: CustomEmailId,
        customEmailBreaches: List<BreachEmail>
    ) {
        customEmailBreachesCache.put(Pair(userId, customEmailId), customEmailBreaches)
            .also { emitCustomEmailBreachesChanges() }
    }

    private fun emitCustomEmailsChanges() {
        customEmailsFlow.tryEmit(customEmailsCache)
    }

    private fun emitCustomEmailBreachesChanges() {
        customEmailBreachesFlow.tryEmit(customEmailBreachesCache)
    }

    override suspend fun getProtonEmail(userId: UserId, addressId: AddressId): BreachProtonEmail =
        protonEmailsCache[Pair(userId, addressId)]
            ?: throw IllegalArgumentException("There's no proton email with id: ${addressId.id}")

    override fun observeProtonEmail(userId: UserId, addressId: AddressId): Flow<BreachProtonEmail> = protonEmailsFlow
        .map { protonEmailsMap -> protonEmailsMap[Pair(userId, addressId)] }
        .filterNotNull()

    override suspend fun upsertProtonEmail(userId: UserId, protonEmail: BreachProtonEmail) {
        protonEmailsCache
            .put(Pair(userId, protonEmail.addressId), protonEmail)
            .also { emitProtonEmailsChanges() }
    }

    override fun observeProtonEmails(userId: UserId): Flow<List<BreachProtonEmail>> = protonEmailsFlow
        .map { protonEmailsMap ->
            protonEmailsMap.filter { (key, _) ->
                key.first == userId
            }.values.toList()
        }

    override suspend fun upsertProtonEmails(userId: UserId, protonEmails: List<BreachProtonEmail>) {
        protonEmails.toSet()
            .forEach { protonEmail ->
                protonEmailsCache[Pair(userId, protonEmail.addressId)] = protonEmail
            }
            .also { emitProtonEmailsChanges() }
    }

    override fun observeProtonEmailBreaches(userId: UserId, addressId: AddressId): Flow<List<BreachEmail>> =
        protonEmailBreachesFlow.map { protonEmailBreachesMap ->
            protonEmailBreachesMap.getOrElse(Pair(userId, addressId)) { emptyList() }
        }

    override suspend fun upsertProtonEmailBreaches(
        userId: UserId,
        addressId: AddressId,
        protonEmailBreaches: List<BreachEmail>
    ) {
        protonEmailBreachesCache.put(Pair(userId, addressId), protonEmailBreaches)
            .also { emitProtonEmailBreachesChanges() }
    }

    private fun emitProtonEmailsChanges() {
        protonEmailsFlow.tryEmit(protonEmailsCache)
    }

    private fun emitProtonEmailBreachesChanges() {
        protonEmailBreachesFlow.tryEmit(protonEmailBreachesCache)
    }

    override fun observeAliasEmailBreaches(userId: UserId, aliasEmailId: AliasEmailId): Flow<List<BreachEmail>> =
        aliasEmailBreachesFlow
            .onStart { emitAliasEmailBreachesChanges() }
            .map { aliasEmailBreachesMap ->
                aliasEmailBreachesMap.getOrElse(Pair(userId, aliasEmailId)) { emptyList() }
            }

    override suspend fun upsertAliasEmailBreaches(
        userId: UserId,
        aliasEmailId: AliasEmailId,
        aliasEmailBreaches: List<BreachEmail>
    ) {
        aliasEmailBreachesCache.put(Pair(userId, aliasEmailId), aliasEmailBreaches)
            .also { emitAliasEmailBreachesChanges() }
    }

    override fun getAliasEmailBreaches(userId: UserId, aliasEmailId: AliasEmailId): List<BreachEmail> =
        aliasEmailBreachesCache[Pair(userId, aliasEmailId)]
            ?: throw IllegalArgumentException("There's no alias email with id: ${aliasEmailId.itemId}")

    override fun getCustomEmailBreaches(userId: UserId, customEmailId: CustomEmailId): List<BreachEmail> =
        customEmailBreachesCache[Pair(userId, customEmailId)]
            ?: throw IllegalArgumentException("There's no custom email with id: ${customEmailId.id}")

    override fun getProtonEmailBreaches(userId: UserId, id: AddressId): List<BreachEmail> =
        protonEmailBreachesCache[Pair(userId, id)]
            ?: throw IllegalArgumentException("There's no proton email with id: ${id.id}")

    private fun emitAliasEmailBreachesChanges() {
        aliasEmailBreachesFlow.tryEmit(aliasEmailBreachesCache)
    }

}

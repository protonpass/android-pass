/*
 * Copyright (c) 2025 Proton AG
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.entities.BreachEmailEntity
import proton.android.pass.data.impl.db.mappers.toDomain
import proton.android.pass.data.impl.db.mappers.toEntity
import proton.android.pass.domain.breach.AliasEmailId
import proton.android.pass.domain.breach.BreachCustomEmail
import proton.android.pass.domain.breach.BreachDomainPeek
import proton.android.pass.domain.breach.BreachEmail
import proton.android.pass.domain.breach.BreachProtonEmail
import proton.android.pass.domain.breach.CustomEmailId
import javax.inject.Inject

class LocalBreachDataSourceImpl @Inject constructor(
    private val database: PassDatabase
) : LocalBreachDataSource {

    override suspend fun getCustomEmail(userId: UserId, customEmailId: CustomEmailId): BreachCustomEmail =
        database.breachCustomEmailDao()
            .observeByUserIdAndId(userId.id, customEmailId.id)
            .first()
            ?.toDomain()
            ?: throw IllegalArgumentException("There's no custom email with id: ${customEmailId.id}")

    override fun observeCustomEmail(userId: UserId, customEmailId: CustomEmailId): Flow<BreachCustomEmail> =
        database.breachCustomEmailDao()
            .observeByUserIdAndId(userId.id, customEmailId.id)
            .map { it?.toDomain() }
            .map { it ?: throw IllegalArgumentException("There's no custom email with id: ${customEmailId.id}") }

    override suspend fun upsertCustomEmail(userId: UserId, customEmail: BreachCustomEmail) {
        val existingEmails = database.breachCustomEmailDao()
            .observeByUserId(userId.id)
            .first()
        existingEmails
            .firstOrNull { it.email == customEmail.email }
            ?.let { existing ->
                database.breachCustomEmailDao().delete(userId.id, existing.customEmailId)
            }

        database.breachCustomEmailDao().upsert(customEmail.toEntity(userId.id))
    }

    override fun observeCustomEmails(): Flow<List<BreachCustomEmail>> = database.breachCustomEmailDao()
        .observeAll()
        .map { entities -> entities.map { it.toDomain() } }

    override fun observeCustomEmails(userId: UserId): Flow<List<BreachCustomEmail>> = database.breachCustomEmailDao()
        .observeByUserId(userId.id)
        .map { entities -> entities.map { it.toDomain() } }

    override suspend fun upsertCustomEmails(userId: UserId, customEmails: List<BreachCustomEmail>) {
        database.breachCustomEmailDao().upsertAll(
            customEmails.map { it.toEntity(userId.id) }
        )
    }

    override suspend fun deleteCustomEmail(userId: UserId, customEmailId: CustomEmailId) {
        database.breachCustomEmailDao().delete(userId.id, customEmailId.id)
    }

    override fun observeCustomEmailBreaches(userId: UserId, customEmailId: CustomEmailId): Flow<List<BreachEmail>> =
        database.breachEmailDao()
            .observeByOwner(
                userId = userId.id,
                emailType = BreachEmailEntity.EMAIL_TYPE_CUSTOM,
                emailOwnerId = customEmailId.id
            )
            .map { entities -> entities.map { it.toDomain() } }

    override suspend fun upsertCustomEmailBreaches(
        userId: UserId,
        customEmailId: CustomEmailId,
        customEmailBreaches: List<BreachEmail>
    ) {
        database.breachEmailDao().deleteByOwner(
            userId = userId.id,
            emailType = BreachEmailEntity.EMAIL_TYPE_CUSTOM,
            emailOwnerId = customEmailId.id
        )
        if (customEmailBreaches.isNotEmpty()) {
            database.breachEmailDao().upsertAll(
                customEmailBreaches.map { it.toEntity(userId.id) }
            )
        }
    }

    override suspend fun getProtonEmail(userId: UserId, addressId: AddressId): BreachProtonEmail =
        database.breachProtonEmailDao()
            .observeByUserIdAndId(userId.id, addressId.id)
            .first()
            ?.toDomain()
            ?: throw IllegalArgumentException("There's no proton email with id: ${addressId.id}")

    override fun observeProtonEmail(userId: UserId, addressId: AddressId): Flow<BreachProtonEmail> =
        database.breachProtonEmailDao()
            .observeByUserIdAndId(userId.id, addressId.id)
            .map { it?.toDomain() }
            .map { it ?: throw IllegalArgumentException("There's no proton email with id: ${addressId.id}") }

    override suspend fun upsertProtonEmail(userId: UserId, protonEmail: BreachProtonEmail) {
        database.breachProtonEmailDao().upsert(protonEmail.toEntity(userId.id))
    }

    override fun observeProtonEmails(userId: UserId): Flow<List<BreachProtonEmail>> = database.breachProtonEmailDao()
        .observeByUserId(userId.id)
        .map { entities -> entities.map { it.toDomain() } }

    override suspend fun upsertProtonEmails(userId: UserId, protonEmails: List<BreachProtonEmail>) {
        database.breachProtonEmailDao().upsertAll(
            protonEmails.map { it.toEntity(userId.id) }
        )
    }

    override fun observeProtonEmailBreaches(userId: UserId, addressId: AddressId): Flow<List<BreachEmail>> =
        database.breachEmailDao()
            .observeByOwner(
                userId = userId.id,
                emailType = BreachEmailEntity.EMAIL_TYPE_PROTON,
                emailOwnerId = addressId.id
            )
            .map { entities -> entities.map { it.toDomain() } }

    override suspend fun upsertProtonEmailBreaches(
        userId: UserId,
        addressId: AddressId,
        protonEmailBreaches: List<BreachEmail>
    ) {
        database.breachEmailDao().deleteByOwner(
            userId = userId.id,
            emailType = BreachEmailEntity.EMAIL_TYPE_PROTON,
            emailOwnerId = addressId.id
        )
        if (protonEmailBreaches.isNotEmpty()) {
            database.breachEmailDao().upsertAll(
                protonEmailBreaches.map { it.toEntity(userId.id) }
            )
        }
    }

    override fun observeAliasEmailBreaches(userId: UserId, aliasEmailId: AliasEmailId): Flow<List<BreachEmail>> =
        database.breachEmailDao()
            .observeByAlias(userId.id, aliasEmailId.shareId.id, aliasEmailId.itemId.id)
            .map { entities -> entities.map { it.toDomain() } }

    override fun observeAllAliasEmailBreaches(userId: UserId): Flow<List<BreachEmail>> = database.breachEmailDao()
        .observeAllAliasEmailBreaches(userId.id)
        .map { entities -> entities.map { it.toDomain() } }

    override suspend fun upsertAliasEmailBreaches(
        userId: UserId,
        aliasEmailId: AliasEmailId,
        aliasEmailBreaches: List<BreachEmail>
    ) {
        database.breachEmailDao().deleteByAlias(
            userId = userId.id,
            shareId = aliasEmailId.shareId.id,
            itemId = aliasEmailId.itemId.id
        )
        if (aliasEmailBreaches.isNotEmpty()) {
            database.breachEmailDao().upsertAll(
                aliasEmailBreaches.map { it.toEntity(userId.id) }
            )
        }
    }

    override suspend fun getAliasEmailBreaches(userId: UserId, aliasEmailId: AliasEmailId): List<BreachEmail> =
        database.breachEmailDao()
            .observeByAlias(userId.id, aliasEmailId.shareId.id, aliasEmailId.itemId.id)
            .first()
            .map { it.toDomain() }

    override suspend fun getCustomEmailBreaches(userId: UserId, customEmailId: CustomEmailId): List<BreachEmail> =
        database.breachEmailDao()
            .observeByOwner(
                userId = userId.id,
                emailType = BreachEmailEntity.EMAIL_TYPE_CUSTOM,
                emailOwnerId = customEmailId.id
            )
            .first()
            .map { it.toDomain() }

    override suspend fun getProtonEmailBreaches(userId: UserId, id: AddressId): List<BreachEmail> =
        database.breachEmailDao()
            .observeByOwner(
                userId = userId.id,
                emailType = BreachEmailEntity.EMAIL_TYPE_PROTON,
                emailOwnerId = id.id
            )
            .first()
            .map { it.toDomain() }

    override fun observeBreachDomainPeeks(userId: UserId): Flow<List<BreachDomainPeek>> = database.breachDomainPeekDao()
        .observeByUserId(userId.id)
        .map { entities -> entities.map { it.toDomain() } }

    override suspend fun upsertBreachDomainPeeks(userId: UserId, domainPeeks: List<BreachDomainPeek>) {
        database.inTransaction(name = "upsertBreachDomainPeeks") {
            database.breachDomainPeekDao().deleteAllForUser(userId.id)
            if (domainPeeks.isNotEmpty()) {
                database.breachDomainPeekDao().upsertAll(
                    domainPeeks.map { it.toEntity(userId.id) }
                )
            }
        }
    }

    override fun observeTotalBreachCount(userId: UserId): Flow<Int> = combine(
        database.breachCustomEmailDao().observeTotalBreachCount(userId.id),
        database.breachProtonEmailDao().observeTotalBreachCount(userId.id)
    ) { customCount, protonCount ->
        (customCount ?: 0) + (protonCount ?: 0)
    }
}

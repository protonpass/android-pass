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

package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.OrganizationSettingsRepository
import proton.android.pass.data.impl.db.entities.PassOrganizationSettingsEntity
import proton.android.pass.data.impl.local.LocalOrganizationSettingsDataSource
import proton.android.pass.data.impl.remote.RemoteOrganizationSettingsDataSource
import proton.android.pass.data.impl.responses.OrganizationGetOrganization
import proton.android.pass.domain.ForceLockSeconds
import proton.android.pass.domain.OrganizationSettings
import proton.android.pass.domain.OrganizationShareMode
import proton.android.pass.domain.organizations.OrganizationPasswordPolicy
import javax.inject.Inject

class OrganizationSettingsRepositoryImpl @Inject constructor(
    private val local: LocalOrganizationSettingsDataSource,
    private val remote: RemoteOrganizationSettingsDataSource
) : OrganizationSettingsRepository {

    override fun observe(userId: UserId): Flow<OrganizationSettings?> = local.observe(userId).map { it?.toDomain() }

    override suspend fun refresh(userId: UserId) {
        val response = remote.request(userId)
        val entity = response.toEntity(userId)
        local.upsert(entity)
    }

    private fun OrganizationGetOrganization?.toEntity(userId: UserId) = when (this) {
        null -> PassOrganizationSettingsEntity.empty(userId.id)
        else -> PassOrganizationSettingsEntity(
            userId = userId.id,
            hasOrganization = true,
            canUpdate = canUpdate,
            shareMode = settings?.shareMode ?: OrganizationShareMode.Unrestricted.value,
            forceLockSeconds = settings?.forceLockSeconds ?: 0,
            randomPasswordAllowed = settings?.passwordPolicy?.randomPasswordAllowed,
            randomPasswordMinLength = settings?.passwordPolicy?.randomPasswordMinLength,
            randomPasswordMaxLength = settings?.passwordPolicy?.randomPasswordMaxLength,
            randomPasswordIncludeNumbers = settings?.passwordPolicy?.randomPasswordMustIncludeNumbers,
            randomPasswordIncludeSymbols = settings?.passwordPolicy?.randomPasswordMustIncludeSymbols,
            randomPasswordIncludeUppercase = settings?.passwordPolicy?.randomPasswordMustIncludeUppercase,
            memorablePasswordAllowed = settings?.passwordPolicy?.memorablePasswordAllowed,
            memorablePasswordMinWords = settings?.passwordPolicy?.memorablePasswordMinWords,
            memorablePasswordMaxWords = settings?.passwordPolicy?.memorablePasswordMaxWords,
            memorablePasswordCapitalize = settings?.passwordPolicy?.memorablePasswordMustCapitalize,
            memorablePasswordIncludeNumbers = settings?.passwordPolicy?.memorablePasswordMustIncludeNumbers
        )
    }

    private fun PassOrganizationSettingsEntity.toDomain() = if (hasOrganization) {
        OrganizationSettings.Organization(
            canUpdate = canUpdate,
            shareMode = OrganizationShareMode.fromValue(shareMode),
            forceLockSeconds = ForceLockSeconds.fromValue(forceLockSeconds),
            passwordPolicy = OrganizationPasswordPolicy(
                randomPasswordAllowed = randomPasswordAllowed,
                randomPasswordMinLength = randomPasswordMinLength,
                randomPasswordMaxLength = randomPasswordMaxLength,
                randomPasswordIncludeNumbers = randomPasswordIncludeNumbers,
                randomPasswordIncludeSymbols = randomPasswordIncludeSymbols,
                randomPasswordIncludeUppercase = randomPasswordIncludeUppercase,
                memorablePasswordAllowed = memorablePasswordAllowed,
                memorablePasswordMinWords = memorablePasswordMinWords,
                memorablePasswordMaxWords = memorablePasswordMaxWords,
                memorablePasswordCapitalize = memorablePasswordCapitalize,
                memorablePasswordIncludeNumbers = memorablePasswordIncludeNumbers
            )
        )
    } else {
        OrganizationSettings.NotAnOrganization
    }
}

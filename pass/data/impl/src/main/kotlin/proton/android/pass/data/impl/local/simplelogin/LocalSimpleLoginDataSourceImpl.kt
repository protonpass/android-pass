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

package proton.android.pass.data.impl.local.simplelogin

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import me.proton.core.domain.entity.UserId
import proton.android.pass.domain.simplelogin.SimpleLoginAliasDomain
import proton.android.pass.domain.simplelogin.SimpleLoginAliasMailbox
import proton.android.pass.domain.simplelogin.SimpleLoginAliasSettings
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.simplelogin.SimpleLoginSyncStatusPreference
import javax.inject.Inject

class LocalSimpleLoginDataSourceImpl @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : LocalSimpleLoginDataSource {

    private val aliasSettingsFlow = MutableStateFlow<SimpleLoginAliasSettings?>(null)

    private val aliasDomainsFlow =
        MutableStateFlow<MutableMap<UserId, List<SimpleLoginAliasDomain>>>(
            value = mutableMapOf()
        )

    private val aliasMailboxesFlow =
        MutableStateFlow<MutableMap<UserId, Map<Long, SimpleLoginAliasMailbox>>>(
            value = mutableMapOf()
        )

    override fun disableSyncPreference() {
        userPreferencesRepository.setSimpleLoginSyncStatusPreference(
            preference = SimpleLoginSyncStatusPreference.Disabled
        )
    }

    override fun observeSyncPreference(): Flow<Boolean> = userPreferencesRepository
        .observeSimpleLoginSyncStatusPreference()
        .map { simpleLoginSyncStatusPreference -> simpleLoginSyncStatusPreference.value }

    override fun observeAliasSettings(): Flow<SimpleLoginAliasSettings> = aliasSettingsFlow
        .filterNotNull()

    override fun updateAliasSettings(newAliasSettings: SimpleLoginAliasSettings) {
        aliasSettingsFlow.update { newAliasSettings }
    }

    override fun observeAliasDomains(userId: UserId): Flow<List<SimpleLoginAliasDomain>> =
        aliasDomainsFlow.mapLatest { aliasDomainsMap ->
            aliasDomainsMap[userId] ?: emptyList()
        }

    override fun refreshAliasDomains(userId: UserId, aliasDomains: List<SimpleLoginAliasDomain>) {
        aliasDomainsFlow.update { aliasDomainsMap ->
            aliasDomainsMap.toMutableMap().apply {
                put(userId, aliasDomains)
            }
        }
    }

    override fun updateDefaultAliasDomain(userId: UserId, newDefaultAliasDomain: String?) {
        aliasDomainsFlow.update { aliasDomainsMap ->
            aliasDomainsMap[userId]?.let { aliasDomains ->
                aliasDomains
                    .map { aliasDomain ->
                        if (aliasDomain.isDefault || aliasDomain.domain == newDefaultAliasDomain) {
                            aliasDomain.copy(isDefault = aliasDomain.domain == newDefaultAliasDomain)
                        } else {
                            aliasDomain
                        }
                    }
                    .let { newAliasDomains ->
                        aliasDomainsMap.toMutableMap().apply {
                            put(userId, newAliasDomains)
                        }
                    }
            } ?: aliasDomainsMap
        }
    }

    override fun observeAliasMailbox(userId: UserId, mailboxId: Long): Flow<SimpleLoginAliasMailbox?> =
        aliasMailboxesFlow.mapLatest { aliasMailboxesMap ->
            aliasMailboxesMap[userId]?.get(mailboxId)
        }

    override fun observeAliasMailboxes(userId: UserId): Flow<List<SimpleLoginAliasMailbox>> =
        aliasMailboxesFlow.mapLatest { aliasMailboxesMap ->
            aliasMailboxesMap[userId]?.values?.toList() ?: emptyList()
        }

    override fun refreshAliasMailboxes(userId: UserId, aliasMailboxes: List<SimpleLoginAliasMailbox>) {
        aliasMailboxesFlow.update { aliasMailboxesMap ->
            aliasMailboxesMap.toMutableMap().apply {
                put(userId, aliasMailboxes.associateBy { aliasMailbox -> aliasMailbox.id })
            }
        }
    }

}

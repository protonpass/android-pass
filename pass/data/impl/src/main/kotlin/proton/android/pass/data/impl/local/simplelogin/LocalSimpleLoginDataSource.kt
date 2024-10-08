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
import me.proton.core.domain.entity.UserId
import proton.android.pass.domain.simplelogin.SimpleLoginAliasDomain
import proton.android.pass.domain.simplelogin.SimpleLoginAliasSettings

interface LocalSimpleLoginDataSource {

    fun disableSyncPreference()

    fun observeSyncPreference(): Flow<Boolean>

    fun observeAliasSettings(): Flow<SimpleLoginAliasSettings>

    fun updateAliasSettings(newAliasSettings: SimpleLoginAliasSettings)

    fun observeAliasDomains(userId: UserId): Flow<List<SimpleLoginAliasDomain>>

    fun refreshAliasDomains(userId: UserId, aliasDomains: List<SimpleLoginAliasDomain>)

}

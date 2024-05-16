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

package proton.android.pass.data.impl.core.datasources

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import proton.android.pass.data.api.core.datasources.LocalSentinelDataSource
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.sentinel.SentinelStatusPreference
import javax.inject.Inject

class LocalSentinelDataSourceImpl @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : LocalSentinelDataSource {

    private val _canEnableSentinel = MutableStateFlow<Boolean?>(null)

    override fun updateCanEnableSentinel(value: Boolean) {
        _canEnableSentinel.value = value
    }

    override fun disableSentinel() {
        userPreferencesRepository.setSentinelStatusPreference(SentinelStatusPreference.Disabled)
    }

    override fun enableSentinel() {
        userPreferencesRepository.setSentinelStatusPreference(SentinelStatusPreference.Enabled)
    }

    override fun observeCanEnableSentinel(): Flow<Boolean?> = _canEnableSentinel.asStateFlow()

    override fun observeIsSentinelEnabled(): Flow<Boolean> = userPreferencesRepository
        .observeSentinelStatusPreference()
        .map { sentinelStatusPreference -> sentinelStatusPreference.value }

}

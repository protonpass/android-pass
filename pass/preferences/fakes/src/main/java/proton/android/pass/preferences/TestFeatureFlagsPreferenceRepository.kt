/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.preferences

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import proton.android.pass.preferences.FeatureFlag.ADVANCED_ALIAS_MANAGEMENT_V1
import proton.android.pass.preferences.FeatureFlag.AUTOFILL_DEBUG_MODE
import proton.android.pass.preferences.FeatureFlag.DIGITAL_ASSET_LINKS
import proton.android.pass.preferences.FeatureFlag.EXTRA_LOGGING
import proton.android.pass.preferences.FeatureFlag.FILE_ATTACHMENTS_V1
import proton.android.pass.preferences.FeatureFlag.IN_APP_MESSAGES_V1
import proton.android.pass.preferences.FeatureFlag.ITEM_SHARING_V1
import proton.android.pass.preferences.FeatureFlag.SL_ALIASES_SYNC
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestFeatureFlagsPreferenceRepository @Inject constructor() :
    FeatureFlagsPreferencesRepository {

    private val state: MutableStateFlow<MutableMap<FeatureFlag, Any?>> =
        MutableStateFlow(mutableMapOf())

    @Suppress("UNCHECKED_CAST")
    override fun <T> get(featureFlag: FeatureFlag): Flow<T> = state.map {
        when (featureFlag) {
            AUTOFILL_DEBUG_MODE -> it.getOrDefault(AUTOFILL_DEBUG_MODE, false) as T
            SL_ALIASES_SYNC -> it.getOrDefault(SL_ALIASES_SYNC, false) as T
            DIGITAL_ASSET_LINKS -> it.getOrDefault(DIGITAL_ASSET_LINKS, false) as T
            ADVANCED_ALIAS_MANAGEMENT_V1 -> it.getOrDefault(ADVANCED_ALIAS_MANAGEMENT_V1, false) as T
            ITEM_SHARING_V1 -> it.getOrDefault(ITEM_SHARING_V1, false) as T
            IN_APP_MESSAGES_V1 -> it.getOrDefault(IN_APP_MESSAGES_V1, false) as T
            EXTRA_LOGGING -> it.getOrDefault(EXTRA_LOGGING, false) as T
            FILE_ATTACHMENTS_V1 -> it.getOrDefault(FILE_ATTACHMENTS_V1, false) as T
        }
    }

    override fun <T> set(featureFlag: FeatureFlag, value: T?): Result<Unit> {
        state.update {
            it[featureFlag] = value
            it
        }
        return Result.success(Unit)
    }

    override fun observeForAllUsers(featureFlag: FeatureFlag): Flow<Boolean> = flowOf(false)

}

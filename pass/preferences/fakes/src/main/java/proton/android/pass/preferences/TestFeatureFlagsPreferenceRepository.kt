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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import proton.android.pass.preferences.FeatureFlag.AUTOFILL_DEBUG_MODE
import proton.android.pass.preferences.FeatureFlag.IDENTITY_V1
import proton.android.pass.preferences.FeatureFlag.SECURITY_CENTER_V1
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
            SECURITY_CENTER_V1 -> it.getOrDefault(SECURITY_CENTER_V1, false) as T
            IDENTITY_V1 -> it.getOrDefault(IDENTITY_V1, false) as T
        }
    }

    override fun <T> set(featureFlag: FeatureFlag, value: T?): Result<Unit> {
        state.update {
            it[featureFlag] = value
            it
        }
        return Result.success(Unit)
    }

}

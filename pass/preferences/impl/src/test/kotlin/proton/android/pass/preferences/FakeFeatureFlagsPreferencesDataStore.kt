/*
 * Copyright (c) 2026 Proton AG
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

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeFeatureFlagsPreferencesDataStore : DataStore<FeatureFlagsPreferences> {

    private val state = MutableStateFlow(FeatureFlagsPreferences.getDefaultInstance())

    override val data: Flow<FeatureFlagsPreferences> = state

    override suspend fun updateData(
        transform: suspend (t: FeatureFlagsPreferences) -> FeatureFlagsPreferences
    ): FeatureFlagsPreferences {
        val updated = transform(state.value)
        state.value = updated
        return updated
    }
}

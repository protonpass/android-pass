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

package proton.android.pass.preferences.tooltips

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import me.proton.android.pass.preferences.BooleanPrefProto
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.TooltipsPreferences
import proton.android.pass.preferences.fromBooleanPrefProto
import proton.android.pass.preferences.toBooleanPrefProto
import java.io.IOException
import javax.inject.Inject

internal class TooltipPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<TooltipsPreferences>
) : TooltipPreferencesRepository {

    override fun observeEnabled(preference: TooltipPreference): Flow<Boolean> = when (preference) {
        TooltipPreference.UsernameSplit -> observeTooltipPreference { usernameSplitEnabled }
    }

    private fun observeTooltipPreference(
        tooltipsPreferenceGetter: TooltipsPreferences.() -> BooleanPrefProto
    ): Flow<Boolean> = dataStore.data
        .catch { error ->
            if (error is IOException) {
                PassLogger.w(TAG, "There was an error reading tooltips preferences.")
                PassLogger.w(TAG, error)
                TooltipsPreferences.getDefaultInstance()
                    .also { tooltipsPreferences -> emit(tooltipsPreferences) }
            } else {
                throw error
            }
        }
        .map { tooltipsPreferences ->
            fromBooleanPrefProto(
                pref = tooltipsPreferenceGetter(tooltipsPreferences),
                default = true
            )
        }

    override suspend fun setEnabled(preference: TooltipPreference, isEnabled: Boolean) {
        isEnabled.toBooleanPrefProto().also { boolFlagPrefProto ->
            when (preference) {
                TooltipPreference.UsernameSplit -> setTooltipPreference {
                    usernameSplitEnabled = boolFlagPrefProto
                }
            }
        }
    }

    override suspend fun clear() = setTooltipPreference {
        clear()
    }

    private suspend fun setTooltipPreference(tooltipsPreferenceSetter: TooltipsPreferences.Builder.() -> Unit) {
        dataStore.updateData { tooltipsPreferences ->
            tooltipsPreferences
                .toBuilder()
                .apply(tooltipsPreferenceSetter)
                .build()
        }
    }

    private companion object {

        private const val TAG = "TooltipPreferencesRepositoryImpl"

    }

}

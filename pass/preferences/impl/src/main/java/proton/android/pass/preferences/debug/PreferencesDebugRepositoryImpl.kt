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

package proton.android.pass.preferences.debug

import androidx.datastore.core.DataStore
import com.google.protobuf.MessageLite
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import proton.android.pass.preferences.InternalSettings
import proton.android.pass.preferences.TooltipsPreferences
import proton.android.pass.preferences.UserPreferences
import javax.inject.Inject

class PreferencesDebugRepositoryImpl @Inject constructor(
    private val userPreferences: DataStore<UserPreferences>,
    private val internalSettings: DataStore<InternalSettings>,
    private val tooltipsPreferences: DataStore<TooltipsPreferences>
) : PreferencesDebugRepository {

    override fun observeDumps(): Flow<List<PreferencesStoreDump>> = combine(
        dumpOf("Internal settings", "internal_settings.pb", internalSettings),
        dumpOf("User preferences", "user_preferences.pb", userPreferences),
        dumpOf("Tooltips", "tooltips_preferences.pb", tooltipsPreferences)
    ) { dumps -> dumps.toList() }

    private fun dumpOf(
        storeName: String,
        fileName: String,
        dataStore: DataStore<out MessageLite>
    ): Flow<PreferencesStoreDump> = dataStore.data
        .map { message -> message.toString().withoutIdentityHeader() }
        .catch { error -> emit("Could not be read: ${error::class.simpleName}: ${error.message}") }
        .map { contents ->
            PreferencesStoreDump(
                storeName = storeName,
                fileName = fileName,
                contents = contents.ifBlank { EMPTY_STORE }
            )
        }

    // Lite's toString() opens with "# <class>@<hash>", which says nothing about the stored values
    private fun String.withoutIdentityHeader(): String = when {
        startsWith(IDENTITY_HEADER_PREFIX) -> substringAfter(LINE_BREAK, "").trim()
        else -> trim()
    }

    private companion object {

        private const val EMPTY_STORE = "(all fields at their default value)"
        private const val IDENTITY_HEADER_PREFIX = "# "
        private const val LINE_BREAK = "\n"

    }
}

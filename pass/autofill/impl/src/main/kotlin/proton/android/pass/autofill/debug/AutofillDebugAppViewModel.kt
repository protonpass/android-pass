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

package proton.android.pass.autofill.debug

import android.content.Context
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import proton.android.pass.preferences.ThemePreference
import proton.android.pass.preferences.UserPreferencesRepository
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AutofillDebugAppViewModel @Inject constructor(
    @ApplicationContext context: Context,
    preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val state: StateFlow<DebugAppUiState> = combine(
        getSessions(context),
        preferencesRepository.getThemePreference()
    ) { sessions, theme ->
        DebugAppUiState(theme, sessions)
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = runBlocking {
                DebugAppUiState(
                    theme = preferencesRepository.getThemePreference().first(),
                    sessions = persistentListOf()
                )
            }
        )

    private fun getSessions(context: Context): Flow<ImmutableList<AutofillSession>> = flow {
        val dir = File(context.cacheDir, "autofill")
        dir.mkdirs()

        val filenames = dir.listFiles()?.map { it.name } ?: emptyList()
        val asSessions = filenames.mapNotNull { filename ->
            val withoutExtension = filename.removeSuffix(".json")
            val parts = withoutExtension.split("-")
            if (parts.size != 2) {
                return@mapNotNull null
            }

            val packageName = parts[0]
            val timestampPart = parts[1]

            val parsedTimestamp = timestampPart.toLong()
            val timestamp = Instant.fromEpochMilliseconds(parsedTimestamp)
                .toLocalDateTime(TimeZone.currentSystemDefault())
            val timestampAsString =
                "${timestamp.year}-${timestamp.monthNumber}-${timestamp.dayOfMonth} " +
                    "${timestamp.hour}:${timestamp.minute}:${timestamp.second}"

            AutofillSession(
                packageName = packageName,
                timestamp = timestampAsString,
                filename = filename
            )
        }.sortedByDescending { it.timestamp }

        emit(asSessions.toImmutableList())
    }.flowOn(Dispatchers.IO)

}

@Stable
data class DebugAppUiState(
    val theme: ThemePreference,
    val sessions: ImmutableList<AutofillSession>
)

@Stable
data class AutofillSession(
    val packageName: String,
    val timestamp: String,
    val filename: String
)

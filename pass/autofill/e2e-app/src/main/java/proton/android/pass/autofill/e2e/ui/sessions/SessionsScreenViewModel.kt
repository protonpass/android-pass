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

package proton.android.pass.autofill.e2e.ui.sessions

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Stable
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import proton.android.pass.autofill.debug.DebugUtils
import proton.android.pass.autofill.e2e.BuildConfig
import proton.android.pass.commonui.api.ClassHolder
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SessionsScreenViewModel @Inject constructor(
    @ApplicationContext context: Context
) : ViewModel() {

    private val refreshFlow: MutableStateFlow<Boolean> = MutableStateFlow(true)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val sessionsFlow = refreshFlow
        .filter { it }
        .flatMapLatest { getSessions(context) }
        .onEach { refreshFlow.update { false } }
        .distinctUntilChanged()

    val state: StateFlow<ImmutableList<AutofillSession>> = sessionsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = persistentListOf()
        )

    fun onClearSessions(contextHolder: ClassHolder<Context>) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            contextHolder.get().map {
                DebugUtils.autofillDumpDir(it).deleteRecursively()
            }

            refreshFlow.update { true }
        }
    }

    private fun getSessions(context: Context): Flow<ImmutableList<AutofillSession>> = flow {
        val dir = DebugUtils.autofillDumpDir(context)
        dir.mkdirs()

        while (currentCoroutineContext().isActive) {
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
                    "${timestamp.year}-${zeroPad(timestamp.monthNumber)}-${zeroPad(timestamp.dayOfMonth)} ${
                    zeroPad(timestamp.hour)
                    }:${zeroPad(timestamp.minute)}:${zeroPad(timestamp.second)}"

                AutofillSession(
                    packageName = packageName,
                    timestamp = timestampAsString,
                    filename = filename
                )
            }.sortedByDescending { it.timestamp }

            emit(asSessions.toImmutableList())
        }
    }.flowOn(Dispatchers.IO)

    fun startShareIntent(
        session: AutofillSession,
        context: ClassHolder<Context>
    ) = viewModelScope.launch(Dispatchers.IO) {
        context.get().map { ctx ->
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "*/*"
            val dumpDir = DebugUtils.autofillDumpDir(ctx)
            val sessionFile = File(dumpDir, session.filename)
            val contentUri: Uri = FileProvider.getUriForFile(
                ctx,
                "${BuildConfig.APPLICATION_ID}.fileprovider",
                sessionFile
            )
            intent.putExtra(Intent.EXTRA_STREAM, contentUri)
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            ctx.startActivity(Intent.createChooser(intent, "Share log"))
        }
    }
}

@Suppress("MagicNumber")
private fun zeroPad(number: Int): String {
    return if (number < 10) {
        "0$number"
    } else {
        number.toString()
    }
}

@Stable
data class AutofillSession(
    val packageName: String,
    val timestamp: String,
    val filename: String
)

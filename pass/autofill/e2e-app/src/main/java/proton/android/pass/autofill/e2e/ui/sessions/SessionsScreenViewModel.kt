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
import android.os.FileObserver
import androidx.compose.runtime.Stable
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import proton.android.pass.autofill.debug.DebugUtils
import proton.android.pass.autofill.e2e.BuildConfig
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.commonui.api.ClassHolder
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SessionsScreenViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val isLoadingFlow: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.Loading)
    private val refreshFlow: MutableStateFlow<Boolean> = MutableStateFlow(true)

    private val dirObserver = object : FileObserver(
        DebugUtils.autofillDumpDir(context).absolutePath,
        CREATE or MOVED_TO or CLOSE_WRITE
    ) {
        override fun onEvent(event: Int, path: String?) {
            if (path != null) {
                refreshFlow.update { true }
                isLoadingFlow.update { IsLoadingState.Loading }
            }
        }
    }

    init {
        dirObserver.startWatching()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val sessionsFlow = refreshFlow
        .filter { it }
        .mapLatest { getSessions(context) }
        .onEach {
            refreshFlow.update { false }
            isLoadingFlow.update { IsLoadingState.NotLoading }
        }
        .asLoadingResult()
        .distinctUntilChanged()

    val state: StateFlow<SessionsScreenUiState> = combine(
        sessionsFlow,
        isLoadingFlow
    ) { sessions, isLoading ->
        when (sessions) {
            is LoadingResult.Error -> SessionsScreenUiState.Error(
                sessions.exception.message ?: "Error"
            )

            LoadingResult.Loading -> SessionsScreenUiState.Loading
            is LoadingResult.Success -> SessionsScreenUiState.Content(
                sessions = sessions.data,
                isLoading = isLoading
            )

        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SessionsScreenUiState.Loading
    )

    override fun onCleared() {
        super.onCleared()
        dirObserver.stopWatching()
    }

    fun refresh() = viewModelScope.launch {
        refreshFlow.update { true }
        isLoadingFlow.update { IsLoadingState.Loading }
    }

    fun onClearSessions(contextHolder: ClassHolder<Context>) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            isLoadingFlow.update { IsLoadingState.Loading }
            contextHolder.get().map {
                DebugUtils.autofillDumpDir(it).deleteRecursively()
            }

            refreshFlow.update { true }
        }
    }

    private suspend fun getSessions(context: Context): ImmutableList<AutofillSession> = withContext(Dispatchers.IO) {
        val dir = DebugUtils.autofillDumpDir(context)
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
                "${timestamp.year}-${zeroPad(timestamp.monthNumber)}-${zeroPad(timestamp.dayOfMonth)} ${
                    zeroPad(timestamp.hour)
                }:${zeroPad(timestamp.minute)}:${zeroPad(timestamp.second)}"

            AutofillSession(
                packageName = packageName,
                timestamp = timestampAsString,
                filename = filename
            )
        }.sortedByDescending { it.timestamp }
        asSessions.toImmutableList()
    }

    fun startShareIntent(session: AutofillSession, contextHolder: ClassHolder<Context>) =
        viewModelScope.launch(Dispatchers.IO) {
            contextHolder.get().map { ctx ->
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
                ctx.startActivity(Intent.createChooser(intent, "Share session"))
            }
        }
}

@Stable
sealed interface SessionsScreenUiState {

    fun isLoading(): Boolean

    @Stable
    data object Loading : SessionsScreenUiState {
        override fun isLoading() = true
    }

    @Stable
    @JvmInline
    value class Error(val message: String) : SessionsScreenUiState {
        override fun isLoading() = false
    }

    @Stable
    data class Content(
        val sessions: ImmutableList<AutofillSession>,
        val isLoading: IsLoadingState
    ) : SessionsScreenUiState {
        override fun isLoading() = isLoading.value()
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

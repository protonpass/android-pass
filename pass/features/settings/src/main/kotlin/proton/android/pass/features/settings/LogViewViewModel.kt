/*
 * Copyright (c) 2023-2026 Proton AG
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

package proton.android.pass.features.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.commonui.api.ClassHolder
import proton.android.pass.log.api.LogFileManager
import proton.android.pass.log.api.PassLogger
import proton.android.pass.log.api.ShareLogsUseCase
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

@HiltViewModel
class LogViewViewModel @Inject constructor(
    private val logFileManager: LogFileManager,
    private val accountManager: AccountManager,
    private val shareLogsUseCase: ShareLogsUseCase,
    private val appDispatchers: AppDispatchers
) : ViewModel() {

    private val _state = MutableStateFlow(LogViewUiState())
    val state: StateFlow<LogViewUiState> = _state

    private var currentLogFile: File? = null
    private var previousChunkEndExclusive: Long = 0L
    private var latestKnownFileLength: Long = 0L
    private var pendingTrailingLine: String? = null
    private val stateMutex = Mutex()
    private val lineIdCounter = AtomicLong(0L)

    fun loadLogFile() = viewModelScope.launch(appDispatchers.io) {
        loadLatestLogChunk()
    }

    fun loadOlderLogs() = viewModelScope.launch(appDispatchers.io) {
        val logFile = currentLogFile ?: resolveLogFile()
        val endExclusive = stateMutex.withLock { previousChunkEndExclusive }
        if (!logFile.exists() || endExclusive <= 0L) return@launch

        _state.update { it.copy(isLoadingOlder = true) }
        runCatching {
            readPreviousChunk(
                file = logFile,
                endExclusive = endExclusive,
                maxLines = PAGE_SIZE
            )
        }.onSuccess { chunk ->
            val logLines = assignIds(chunk.lines)
            stateMutex.withLock {
                previousChunkEndExclusive = chunk.nextOlderEndExclusive
            }
            _state.update {
                it.copy(
                    lines = it.lines + logLines,
                    isLoadingOlder = false,
                    hasOlderLogs = chunk.hasOlderLogs
                )
            }
        }.onFailure {
            PassLogger.w(TAG, "Could not load older logs")
            PassLogger.w(TAG, it)
            _state.update { state ->
                state.copy(isLoadingOlder = false)
            }
        }
    }

    fun refreshLogs() = viewModelScope.launch(appDispatchers.io) {
        val logFile = currentLogFile ?: resolveLogFile()
        _state.update { it.copy(isRefreshing = true) }

        runCatching {
            val (startLen, pendingLine) = stateMutex.withLock {
                Pair(latestKnownFileLength, pendingTrailingLine)
            }
            val currentLen = if (logFile.exists()) logFile.length() else -1L
            when {
                currentLen < 0L -> RefreshResult.Reload
                currentLen < startLen -> RefreshResult.Reload
                currentLen == startLen -> RefreshResult.NoChanges
                else -> {
                    // Guard against theoretical Int overflow on very large deltas — reload instead.
                    val delta = currentLen - startLen
                    if (delta > Int.MAX_VALUE) return@runCatching RefreshResult.Reload
                    RefreshResult.NewLines(
                        appendResult = readAppendedLines(
                            file = logFile,
                            startOffset = startLen,
                            endOffset = currentLen,
                            existingPartialLine = pendingLine
                        ),
                        latestLength = currentLen
                    )
                }
            }
        }.onSuccess { result ->
            when (result) {
                RefreshResult.NoChanges -> _state.update { it.copy(isRefreshing = false) }
                // isRefreshing stays true while loadLatestLogChunk executes — intentional.
                RefreshResult.Reload -> loadLatestLogChunk(isRefreshing = false)
                is RefreshResult.NewLines -> {
                    val logLines = assignIds(result.appendResult.lines)
                    stateMutex.withLock {
                        latestKnownFileLength = result.latestLength
                        pendingTrailingLine = result.appendResult.trailingPartialLine
                    }
                    _state.update {
                        it.copy(
                            lines = logLines + it.lines,
                            isRefreshing = false
                        )
                    }
                }
            }
        }.onFailure {
            PassLogger.w(TAG, "Could not refresh logs")
            PassLogger.w(TAG, it)
            _state.update { state ->
                state.copy(isRefreshing = false)
            }
        }
    }

    fun clearLogs() = viewModelScope.launch(appDispatchers.io) {
        try {
            val logFile = currentLogFile ?: resolveLogFile()
            if (logFile.exists()) {
                logFileManager.deleteLogFile(logFile)
            }
            currentLogFile = logFile
            stateMutex.withLock {
                previousChunkEndExclusive = 0L
                latestKnownFileLength = 0L
                pendingTrailingLine = null
            }
            _state.update { LogViewUiState() }
        } catch (e: IOException) {
            PassLogger.w(TAG, "Could not clear log file")
            PassLogger.w(TAG, e)
        }
    }

    fun showClearLogsDialog() {
        _state.update { it.copy(showClearLogsDialog = true) }
    }

    fun dismissClearLogsDialog() {
        _state.update { it.copy(showClearLogsDialog = false) }
    }

    fun startShareIntent(contextHolder: ClassHolder<Context>) = viewModelScope.launch {
        contextHolder.get().value()?.let { context ->
            shareLogsUseCase(context)
        }
    }

    private suspend fun loadLatestLogChunk(isRefreshing: Boolean = false) {
        try {
            val logFile = resolveLogFile()
            currentLogFile = logFile

            if (!logFile.exists()) {
                stateMutex.withLock {
                    previousChunkEndExclusive = 0L
                    latestKnownFileLength = 0L
                }
                _state.update { LogViewUiState() }
                return
            }

            val latestLength = logFile.length()
            val chunk = readPreviousChunk(
                file = logFile,
                endExclusive = latestLength,
                maxLines = PAGE_SIZE
            )

            val visibleLines = if (chunk.trailingPartialLine != null) {
                chunk.lines.drop(1)
            } else {
                chunk.lines
            }

            val logLines = assignIds(visibleLines)
            stateMutex.withLock {
                previousChunkEndExclusive = chunk.nextOlderEndExclusive
                latestKnownFileLength = latestLength
                pendingTrailingLine = chunk.trailingPartialLine
            }
            _state.update {
                LogViewUiState(
                    lines = logLines,
                    isRefreshing = isRefreshing,
                    hasOlderLogs = chunk.hasOlderLogs
                )
            }
        } catch (e: IOException) {
            PassLogger.w(TAG, "Could not read log file")
            PassLogger.w(TAG, e)
            _state.update { LogViewUiState() }
        }
    }

    private suspend fun resolveLogFile(): File {
        val userId = accountManager.getPrimaryUserId().firstOrNull()
        return logFileManager.getLogFile(userId)
    }

    private fun assignIds(lines: List<String>): List<LogLine> =
        lines.map { LogLine(lineIdCounter.getAndIncrement(), it) }

    private sealed interface RefreshResult {
        data object NoChanges : RefreshResult
        data object Reload : RefreshResult
        data class NewLines(val appendResult: AppendResult, val latestLength: Long) : RefreshResult
    }

    companion object {
        private const val PAGE_SIZE = 100
        private const val TAG = "LogViewViewModel"
    }
}

private data class LogChunk(
    val lines: List<String>,
    val nextOlderEndExclusive: Long,
    val hasOlderLogs: Boolean,
    val trailingPartialLine: String?
)

private data class AppendResult(
    val lines: List<String>,
    val trailingPartialLine: String?
)

private fun readPreviousChunk(
    file: File,
    endExclusive: Long,
    maxLines: Int
): LogChunk {
    if (!file.exists() || endExclusive <= 0L || maxLines <= 0) {
        return LogChunk(emptyList(), 0L, false, null)
    }

    RandomAccessFile(file, "r").use { randomAccessFile ->
        var pointer = minOf(endExclusive, randomAccessFile.length()) - 1
        if (pointer < 0) return LogChunk(emptyList(), 0L, false, null)

        val endsWithNewLine = randomAccessFile.readByteAt(pointer) == NEW_LINE
        if (endsWithNewLine) {
            pointer--
        }

        val currentLineBytes = mutableListOf<Byte>()
        val lines = mutableListOf<String>()

        while (pointer >= 0 && lines.size < maxLines) {
            when (val currentByte = randomAccessFile.readByteAt(pointer)) {
                NEW_LINE -> {
                    if (currentLineBytes.isNotEmpty()) {
                        lines += currentLineBytes.reversed().toByteArray().decodeToString()
                        currentLineBytes.clear()
                    }
                }

                CARRIAGE_RETURN -> Unit
                else -> currentLineBytes += currentByte
            }
            pointer--
        }

        if (currentLineBytes.isNotEmpty() && lines.size < maxLines) {
            lines += currentLineBytes.reversed().toByteArray().decodeToString()
        }

        val hasOlderLogs = pointer >= 0
        val trailingPartialLine = if (!endsWithNewLine) lines.firstOrNull() else null
        return LogChunk(
            lines = lines,
            nextOlderEndExclusive = if (hasOlderLogs) pointer + 1 else 0L,
            hasOlderLogs = hasOlderLogs,
            trailingPartialLine = trailingPartialLine
        )
    }
}

private fun readAppendedLines(
    file: File,
    startOffset: Long,
    endOffset: Long,
    existingPartialLine: String?
): AppendResult {
    if (!file.exists() || endOffset <= startOffset) {
        return AppendResult(emptyList(), existingPartialLine)
    }

    RandomAccessFile(file, "r").use { randomAccessFile ->
        randomAccessFile.seek(startOffset)
        val bytes = ByteArray((endOffset - startOffset).toInt())
        randomAccessFile.readFully(bytes)

        val decoded = bytes.decodeToString()
        val endsWithNewLine = decoded.endsWith('\n')
        val parts = decoded.split('\n').map { it.removeSuffix("\r") }

        // Always drop the last element: it is either an empty string produced by a
        // trailing '\n' (endsWithNewLine=true) or an incomplete partial line
        // (endsWithNewLine=false) that must not be emitted as a complete log line yet.
        val completeParts = parts.dropLast(1)
        val mergedParts = completeParts.toMutableList()

        if (existingPartialLine != null) {
            if (mergedParts.isNotEmpty()) {
                mergedParts[0] = existingPartialLine + mergedParts[0]
            } else if (endsWithNewLine) {
                mergedParts += existingPartialLine
            }
        }

        val trailingPartialLine = if (endsWithNewLine) {
            null
        } else {
            val trailing = parts.lastOrNull().orEmpty()
            if (existingPartialLine != null) existingPartialLine + trailing else trailing
        }

        return AppendResult(
            lines = mergedParts.filter { it.isNotEmpty() }.reversed(),
            trailingPartialLine = trailingPartialLine?.takeIf { it.isNotEmpty() }
        )
    }
}

private fun RandomAccessFile.readByteAt(offset: Long): Byte {
    seek(offset)
    return readByte()
}

private const val NEW_LINE: Byte = '\n'.code.toByte()
private const val CARRIAGE_RETURN: Byte = '\r'.code.toByte()

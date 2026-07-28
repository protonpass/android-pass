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

package proton.android.pass.log.impl

import android.annotation.SuppressLint
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.log.api.LogFileManager
import proton.android.pass.log.api.LogoutLogger
import proton.android.pass.log.api.PrivacySanitizer
import timber.log.Timber
import java.io.BufferedWriter
import java.io.File
import java.io.FileNotFoundException
import java.io.FileWriter
import java.io.IOException
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileLoggingTree @Inject constructor(
    private val logFileManager: LogFileManager,
    private val privacySanitizer: PrivacySanitizer,
    private val accountManager: AccountManager,
    appDispatchers: AppDispatchers,
    @param:LogFileMaxSize private val maxFileSize: Long,
    @param:LogRotationLines private val rotationLines: Int,
    private val clock: Clock,
    @LogQueueCapacity queueCapacity: Int
) : Timber.Tree() {

    private val dateTimeFormatter = DateTimeFormatter
        .ofPattern("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH)
        .withZone(ZoneId.from(ZoneOffset.UTC))
    private val scope = CoroutineScope(SupervisorJob() + appDispatchers.io)
    private val entries = Channel<LogEntry>(queueCapacity)

    init {
        scope.launch {
            for (firstEntry in entries) {
                val batch = ArrayList<LogEntry>(DEFAULT_WRITER_BATCH_SIZE)
                batch.add(firstEntry)
                while (batch.size < DEFAULT_WRITER_BATCH_SIZE) {
                    val entry = entries.tryReceive().getOrNull() ?: break
                    batch.add(entry)
                }
                writeBatch(batch)
            }
        }
    }

    private fun shouldRotate(logFile: File) = logFile.length() >= maxFileSize

    @SuppressLint("LogNotTimber")
    private fun rotateLog(logFile: File) {
        val tempFile = File(logFile.parent, "${logFile.name}.tmp")
        try {
            var totalLines = 0
            val recentLines = ArrayDeque<String>(rotationLines)

            logFile.bufferedReader().use { reader ->
                reader.lineSequence().forEach { line ->
                    totalLines++
                    val processedLine = if (line.length > MAX_LOG_LINE_LENGTH) {
                        line.take(MAX_LOG_LINE_LENGTH) + "... [truncated]"
                    } else {
                        line
                    }
                    recentLines.addLast(processedLine)
                    if (recentLines.size > rotationLines) {
                        recentLines.removeFirst()
                    }
                }
            }

            if (totalLines <= rotationLines) {
                return
            }

            tempFile.bufferedWriter().use { writer ->
                recentLines.forEach { line ->
                    writer.write(line)
                    writer.newLine()
                }
            }

            if (!tempFile.exists() || tempFile.length() == 0L) {
                throw IOException("Temp file not created properly")
            }

            var renamed = tempFile.renameTo(logFile)
            if (!renamed) {
                if (logFile.delete()) {
                    renamed = tempFile.renameTo(logFile)
                }
                if (!renamed) {
                    logError("Could not replace log file")
                    throw IOException("Failed to rename temporary log file")
                }
            }
        } catch (ignoredException: IOException) {
            logWarning("Could not rotate file")
            if (ignoredException.message?.contains("rename") != true) {
                tempFile.delete()
            }
        } catch (_: FileNotFoundException) {
            logWarning("Could not find log file")
            tempFile.delete()
        } catch (_: OutOfMemoryError) {
            logError("Out of memory while rotating log file")
            tempFile.delete()
        }
    }

    @SuppressLint("LogNotTimber")
    override fun log(
        priority: Int,
        tag: String?,
        message: String,
        t: Throwable?
    ) {
        if (priority < Log.INFO) return
        val target = if (tag == LogoutLogger.TAG) LogTarget.NotAuthenticated else LogTarget.CurrentUser
        enqueue(LogEntry(buildLog(clock.now(), priority, tag, message), target))
    }

    private fun enqueue(entry: LogEntry) {
        val result = entries.trySend(entry)
        if (result.isFailure && !result.isClosed) {
            logWarning("Log entry dropped because the queue is full")
        }
    }

    @SuppressLint("LogNotTimber")
    private fun logError(message: String) {
        try {
            Log.e(TAG, message)
        } catch (_: Exception) {
            // Logging diagnostics must not prevent subsequent file-log writes.
        }
    }

    @SuppressLint("LogNotTimber")
    private fun logWarning(message: String) {
        try {
            Log.w(TAG, message)
        } catch (_: Exception) {
            // Logging diagnostics must not prevent subsequent file-log writes.
        }
    }

    private suspend fun writeBatch(entries: List<LogEntry>) {
        var writer: BufferedWriter? = null
        var writerFile: File? = null

        fun closeWriter() {
            writer?.let { activeWriter ->
                writer = null
                writerFile = null
                activeWriter.close()
            }
        }

        var cancellationException: CancellationException? = null
        try {
            entries.forEach { entry ->
                logFileManager.withLogFileLock {
                    try {
                        val file = resolveLogFile(entry.target)
                        if (writerFile != file) {
                            closeWriter()
                        }
                        if (shouldRotate(file)) {
                            closeWriter()
                            rotateLog(file)
                        }
                        if (writer == null) {
                            writer = BufferedWriter(FileWriter(file, true))
                            writerFile = file
                        }
                        writer?.apply {
                            append(entry.message)
                            newLine()
                            flush()
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        try {
                            closeWriter()
                        } catch (e: CancellationException) {
                            throw e
                        } catch (_: Exception) {
                            // The original write failure is reported below.
                        }
                        logError("Could not write to log file")
                    }
                }
            }
        } catch (e: CancellationException) {
            cancellationException = e
        } finally {
            withContext(NonCancellable) {
                try {
                    logFileManager.withLogFileLock {
                        closeWriter()
                    }
                } catch (_: Exception) {
                    logError("Could not write to log file")
                }
            }
        }
        cancellationException?.let { throw it }
    }

    private suspend fun resolveLogFile(target: LogTarget): File {
        if (target == LogTarget.NotAuthenticated) {
            return logFileManager.getLogFile(null).also { logFileManager.ensureLogFileExists(it) }
        }
        val initialUserId = accountManager.getPrimaryUserId().firstOrNull()
        val initialFile = logFileManager.getLogFile(initialUserId)
        val (fileUserId, initialEnsuredFile) = ensureCurrentLogFile(initialUserId, initialFile)
        var file = initialEnsuredFile
        val currentUserId = accountManager.getPrimaryUserId().firstOrNull()
        if (currentUserId != fileUserId) {
            file = logFileManager.getLogFile(currentUserId)
        }
        logFileManager.ensureLogFileExists(file)
        return file
    }

    private suspend fun ensureCurrentLogFile(selectedUserId: UserId?, selectedFile: File): Pair<UserId?, File> {
        val currentUserId = accountManager.getPrimaryUserId().firstOrNull()
        val currentFile = if (currentUserId == selectedUserId) {
            selectedFile
        } else {
            logFileManager.getLogFile(currentUserId)
        }
        logFileManager.ensureLogFileExists(currentFile)
        return currentUserId to currentFile
    }

    private fun buildLog(
        timestamp: Instant,
        priority: Int,
        tag: String?,
        message: String
    ): String = buildString {
        append(dateTimeFormatter.format(timestamp.toJavaInstant()))
        append(' ')
        append(priority.toPriorityChar())
        append(": ")
        append(tag ?: "EmptyTag")
        append(" - ")
        val sanitized = privacySanitizer.sanitize(message)
        if (sanitized.length > MAX_LOG_LINE_LENGTH) {
            append(sanitized.take(MAX_LOG_LINE_LENGTH))
            append("... [truncated ${sanitized.length - MAX_LOG_LINE_LENGTH} chars]")
        } else {
            append(sanitized)
        }
    }

    private fun Int.toPriorityChar(): Char = when (this) {
        Log.VERBOSE -> 'V'
        Log.DEBUG -> 'D'
        Log.INFO -> 'I'
        Log.WARN -> 'W'
        Log.ERROR -> 'E'
        Log.ASSERT -> 'A'
        else -> '-'
    }

    private data class LogEntry(
        val message: String,
        val target: LogTarget
    )

    private enum class LogTarget {
        CurrentUser,
        NotAuthenticated
    }

    companion object {
        private const val TAG = "FileLoggingTree"
        private const val MAX_LOG_LINE_LENGTH = 10_000
        internal const val DEFAULT_MAX_FILE_SIZE: Long = 4 * 1024 * 1024
        internal const val DEFAULT_ROTATION_LINES: Int = 500
        internal const val DEFAULT_QUEUE_CAPACITY: Int = 1_024
        internal const val DEFAULT_WRITER_BATCH_SIZE: Int = 64
    }
}

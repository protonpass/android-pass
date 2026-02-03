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

package proton.android.pass.log.impl

import android.annotation.SuppressLint
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.log.api.LogFileManager
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
    @param:LogRotationLines private val rotationLines: Int
) : Timber.Tree() {

    private val mutex = Mutex()
    private val dateTimeFormatter = DateTimeFormatter
        .ofPattern("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH)
        .withZone(ZoneId.from(ZoneOffset.UTC))
    private val scope = CoroutineScope(SupervisorJob() + appDispatchers.io)

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
                    Log.e(TAG, "Failed to replace log file. Temp file preserved at: ${tempFile.absolutePath}")
                    throw IOException("Failed to rename temp file - temp preserved for recovery")
                }
            }
        } catch (e: IOException) {
            Log.w(TAG, "Could not rotate file", e)
            if (e.message?.contains("rename") != true) {
                tempFile.delete()
            }
        } catch (e: FileNotFoundException) {
            Log.w(TAG, "Could not find log file", e)
            tempFile.delete()
        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "Out of memory while rotating log file", e)
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
        scope.launch {
            try {
                val userId = accountManager.getPrimaryUserId().firstOrNull()
                val file = logFileManager.getLogFile(userId)
                logFileManager.ensureLogFileExists(file)

                mutex.withLock {
                    if (shouldRotate(file)) {
                        rotateLog(file)
                    }

                    BufferedWriter(FileWriter(file, true))
                        .use { writer ->
                            writer.append(buildLog(priority, tag, message))
                            writer.newLine()
                            writer.flush()
                        }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Could not write to log file", e)
            }
        }
    }

    private fun buildLog(
        priority: Int,
        tag: String?,
        message: String
    ): String = buildString {
        append(dateTimeFormatter.format(Clock.System.now().toJavaInstant()))
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

    companion object {
        private const val TAG = "FileLoggingTree"
        private const val MAX_LOG_LINE_LENGTH = 10_000
        internal const val DEFAULT_MAX_FILE_SIZE: Long = 4 * 1024 * 1024
        internal const val DEFAULT_ROTATION_LINES: Int = 500
    }
}

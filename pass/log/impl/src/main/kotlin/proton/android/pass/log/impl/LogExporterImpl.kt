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

package proton.android.pass.log.impl

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.LocaleList
import android.os.StatFs
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import proton.android.pass.appconfig.api.AppConfig
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.common.api.FileSizeUtil.toHumanReadableSize
import proton.android.pass.domain.UserId
import proton.android.pass.log.api.LogExporter
import proton.android.pass.log.api.LogFileManager
import proton.android.pass.log.api.PassLogger
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.text.DecimalFormat
import javax.inject.Inject

class LogExporterImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val appConfig: AppConfig,
    private val logFileManager: LogFileManager,
    private val appDispatchers: AppDispatchers
) : LogExporter {

    override suspend fun export(destination: File, userId: UserId?) = withContext<Unit>(appDispatchers.io) {
        val authenticatedLogFile = userId?.let { logFileManager.getLogFile(it) }
        val anonymousLogFile = logFileManager.getLogFile(null)
        authenticatedLogFile?.let { logFileManager.ensureLogFileExists(it) }
        logFileManager.ensureLogFileExists(anonymousLogFile)

        destination.bufferedWriter().use { writer ->
            writer.append(generateDeviceInfo())
            writer.newLine()

            if (anonymousLogFile.exists()) {
                appendMergedLogFiles(
                    writer = writer,
                    authenticatedLogFile = authenticatedLogFile?.takeIf(File::exists),
                    anonymousLogFile = anonymousLogFile
                )
            }
        }
    }

    private fun generateDeviceInfo(): String = runCatching {
        buildString {
            val memory = getMemory()
            val storage = getStorage()
            append("-----------------------------------------")
            append("\n")
            append("PACKAGE:     ${context.packageName}")
            append("\n")
            append("OS:          Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            append("\n")
            append("VERSION:     ${appConfig.versionName}")
            append("\n")
            append("DEVICE:      ${Build.MANUFACTURER} ${Build.MODEL}")
            append("\n")
            append("FINGERPRINT: ${Build.FINGERPRINT}")
            append("\n")
            append("ABI:         ${Build.SUPPORTED_ABIS.joinToString(",")}")
            append("\n")
            append(
                "LOCALE:      ${
                    runCatching { LocaleList.getDefault().toLanguageTags() }
                        .getOrDefault("UNAVAILABLE")
                }"
            )
            append("\n")
            append("MEMORY:      $memory")
            append("\n")
            append("STORAGE:     $storage")
            append("\n")
            append("-----------------------------------------")
        }
    }.getOrElse {
        PassLogger.w(TAG, it)
        "-----------------------------------------\nDEVICE INFO UNAVAILABLE\n-----------------------------------------"
    }

    private fun getStorage(): String {
        val free = freeStorage()
        val total = totalStorage()
        return "Free: ${toHumanReadableSize(free)} | Total: ${toHumanReadableSize(total)}"
    }

    private fun getMemory(): String = runCatching {
        val mi = ActivityManager.MemoryInfo()
        val activityManager = context.getSystemService(ActivityManager::class.java)
            ?: return "UNAVAILABLE"
        activityManager.getMemoryInfo(mi)

        val usedMem = mi.totalMem - mi.availMem
        val percentUsed: Double = usedMem.toDouble() / mi.totalMem.toDouble() * 100

        "Available: ${toHumanReadableSize(mi.availMem)} / ${toHumanReadableSize(mi.totalMem)}" +
            " (${floatForm(percentUsed)}% used)"
    }.getOrDefault("UNAVAILABLE")

    private fun totalStorage(): Long = runCatching {
        val statFs = StatFs(Environment.getDataDirectory().absolutePath)
        statFs.blockCountLong * statFs.blockSizeLong
    }.getOrDefault(0L)

    private fun freeStorage(): Long = runCatching {
        val statFs = StatFs(Environment.getDataDirectory().absolutePath)
        statFs.freeBlocksLong * statFs.blockSizeLong
    }.getOrDefault(0L)

    private fun floatForm(d: Double) = DecimalFormat("#.##").format(d)

    private fun appendMergedLogFiles(
        writer: BufferedWriter,
        authenticatedLogFile: File?,
        anonymousLogFile: File
    ) {
        if (authenticatedLogFile == null) {
            anonymousLogFile.bufferedReader().use { anonymousReader ->
                appendMergedLogLines(
                    writer = writer,
                    authenticatedReader = null,
                    anonymousReader = anonymousReader
                )
            }
            return
        }

        authenticatedLogFile.bufferedReader().use { authenticatedReader ->
            anonymousLogFile.bufferedReader().use { anonymousReader ->
                appendMergedLogLines(writer, authenticatedReader, anonymousReader)
            }
        }
    }

    private fun appendMergedLogLines(
        writer: BufferedWriter,
        authenticatedReader: BufferedReader?,
        anonymousReader: BufferedReader
    ) {
        var firstLine: String? = null
        var previousSource: LogSource? = null
        var previousMessage: String? = null
        var repetitions = 0
        var lastTimestamp: String? = null

        fun writePreviousLine() {
            val line = firstLine ?: return
            writer.write(line.withSourceTag(requireNotNull(previousSource)))
            if (repetitions > 1) {
                writer.write(" [repeated $repetitions times; last at $lastTimestamp]")
            }
            writer.newLine()
        }

        var authenticatedLine = authenticatedReader?.readLine()
        var anonymousLine = anonymousReader.readLine()
        while (authenticatedLine != null || anonymousLine != null) {
            val source: LogSource
            val line: String
            if (shouldWriteAuthenticatedLine(authenticatedLine, anonymousLine)) {
                source = LogSource.Authenticated
                line = requireNotNull(authenticatedLine)
                authenticatedLine = authenticatedReader?.readLine()
            } else {
                source = LogSource.Anonymous
                line = requireNotNull(anonymousLine)
                anonymousLine = anonymousReader.readLine()
            }

            val message = line.messageWithoutTimestamp()
            if (source == previousSource && message != null && message == previousMessage) {
                repetitions++
                lastTimestamp = line.timestampOrNull()
            } else {
                writePreviousLine()
                firstLine = line
                previousSource = source
                previousMessage = message
                repetitions = 1
                lastTimestamp = line.timestampOrNull()
            }
        }
        writePreviousLine()
    }

    private fun shouldWriteAuthenticatedLine(authenticatedLine: String?, anonymousLine: String?): Boolean = when {
        authenticatedLine == null -> false
        anonymousLine == null -> true
        else -> authenticatedLine.timestampOrNull().orEmpty() <=
            anonymousLine.timestampOrNull().orEmpty()
    }

    private fun String.withSourceTag(source: LogSource): String = timestampOrNull()?.let { timestamp ->
        "$timestamp ${source.tag} ${substring(LOG_TIMESTAMP_LENGTH + 1)}"
    } ?: "${source.tag} $this"

    private fun String.messageWithoutTimestamp(): String? = timestampOrNull()?.let {
        substring(LOG_TIMESTAMP_LENGTH + 1)
    }

    private fun String.timestampOrNull(): String? = takeIf {
        length > LOG_TIMESTAMP_LENGTH && it[LOG_TIMESTAMP_LENGTH] == ' '
    }?.take(LOG_TIMESTAMP_LENGTH)

    private enum class LogSource(val tag: String) {
        Authenticated("[AU]"),
        Anonymous("[NA]")
    }

    companion object {
        private const val TAG = "LogExporterImpl"
        private const val LOG_TIMESTAMP_LENGTH = 23
    }
}

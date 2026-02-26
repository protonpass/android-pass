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
import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.LocaleList
import android.os.StatFs
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.appconfig.api.AppConfig
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.common.api.FileSizeUtil.toHumanReadableSize
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.ClassHolder
import proton.android.pass.commonui.api.FileHandler
import proton.android.pass.log.api.LogFileManager
import proton.android.pass.log.api.PassLogger
import proton.android.pass.log.api.ShareLogsConstants
import proton.android.pass.log.api.ShareLogsUseCase
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
import java.text.DecimalFormat
import javax.inject.Inject

class ShareLogsUseCaseImpl @Inject constructor(
    private val appConfig: AppConfig,
    private val logFileManager: LogFileManager,
    private val accountManager: AccountManager,
    private val fileHandler: FileHandler,
    private val appDispatchers: AppDispatchers
) : ShareLogsUseCase {

    @SuppressLint("LogNotTimber")
    override suspend fun invoke(context: Context): Result<File> = runCatching {
        // Prepare file and URI on IO dispatcher
        val (tempFile, tempFileUri) = withContext(appDispatchers.io) {
            val userId = accountManager.getPrimaryUserId().firstOrNull()
            val logFile = logFileManager.getLogFile(userId)
            logFileManager.ensureLogFileExists(logFile)

            // Use share subdirectory which is configured in FileProvider
            val shareDir = File(context.cacheDir, "share")
            shareDir.mkdirs()
            val file = File(shareDir, "pass_logs_${System.currentTimeMillis()}.log")
            PassLogger.i(TAG, "Creating share log file: ${file.absolutePath}")

            file.bufferedWriter().use { writer ->
                writer.append(generateDeviceInfo(context))
                writer.newLine()

                if (logFile.exists()) {
                    logFile.bufferedReader().use { reader ->
                        reader.copyTo(writer)
                    }
                }
            }
            file to file.toURI()
        }

        PassLogger.i(TAG, "Sharing log file: ${tempFile.name} with URI: $tempFileUri")
        fileHandler.shareFileWithEmail(
            contextHolder = ClassHolder(Some(WeakReference(context))),
            uri = tempFileUri,
            mimeType = "text/plain",
            chooserTitle = ShareLogsConstants.CHOOSER_TITLE,
            email = ShareLogsConstants.EMAIL,
            subject = ShareLogsConstants.SUBJECT
        )

        tempFile
    }.onFailure { e ->
        if (e is IOException) {
            PassLogger.w(TAG, "Could not share log file")
            PassLogger.w(TAG, e)
        } else {
            throw e
        }
    }

    private fun generateDeviceInfo(context: Context): String = runCatching {
        buildString {
            val memory = getMemory(context)
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

    private fun getMemory(context: Context): String = runCatching {
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

    companion object {
        private const val TAG = "ShareLogsUseCaseImpl"
    }
}

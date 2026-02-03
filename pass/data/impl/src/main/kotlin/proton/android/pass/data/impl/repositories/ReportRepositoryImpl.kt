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

package proton.android.pass.data.impl.repositories

import android.webkit.MimeTypeMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.network.data.ApiProvider
import me.proton.core.report.domain.entity.BugReportMeta
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import proton.android.pass.data.api.repositories.ReportRepository
import proton.android.pass.data.api.usecases.report.Report
import proton.android.pass.data.impl.core.api.CoreApi
import proton.android.pass.log.api.LogFileManager
import proton.android.pass.log.api.PassLogger
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.inject.Inject
import javax.inject.Provider

class ReportRepositoryImpl @Inject constructor(
    private val apiProvider: ApiProvider,
    private val bugReportMetaProvider: Provider<BugReportMeta>,
    private val logFileManager: LogFileManager,
    private val accountManager: AccountManager
) : ReportRepository {

    private val File.mimeType: String?
        get() = name
            .substringAfterLast('.', "")
            .let { extension ->
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            }

    override suspend fun sendReport(report: Report) {
        apiProvider.get<CoreApi>()
            .invoke {
                val logFile = if (report.shouldAttachLog) {
                    val userId = accountManager.getPrimaryUserId().firstOrNull()
                    logFileManager.getLogFile(userId)
                } else {
                    null
                }
                val logFileSize = logFile?.length() ?: 0
                val extraFilesSize = report.extraFiles.sumOf { it.length() }
                PassLogger.i(TAG, "Log file size: $logFileSize bytes")
                PassLogger.i(TAG, "Total extra files size: $extraFilesSize bytes")
                val body = getMultipartBodyBuilder(
                    report = report,
                    meta = bugReportMetaProvider.get(),
                    logFile = logFile,
                    extraFiles = report.extraFiles
                ).build()
                PassLogger.i(TAG, "Bug report size: ${body.size} bytes")
                val total = logFileSize + extraFilesSize + body.size
                PassLogger.i(TAG, "Total size: $total bytes")
                sendBugReport(body)
            }
            .valueOrThrow
    }

    private suspend fun getMultipartBodyBuilder(
        report: Report,
        meta: BugReportMeta,
        logFile: File?,
        extraFiles: Set<File>
    ): MultipartBody.Builder = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart(name = "OS", value = meta.osName)
        .addFormDataPart(name = "OSVersion", value = meta.osVersion)
        .addFormDataPart(name = "Client", value = meta.clientName)
        .addFormDataPart(name = "ClientVersion", value = meta.appVersionName)
        .addFormDataPart(name = "ClientType", value = "5")
        .addFormDataPart(name = "Title", value = report.title)
        .addFormDataPart(name = "Description", value = report.description)
        .addFormDataPart(name = "Username", value = report.username)
        .addFormDataPart(name = "Email", value = report.email)
        .apply {
            logFile?.validateFile()?.let { file ->
                val tempFile = File.createTempFile(file.nameWithoutExtension, ".${file.extension}")
                Files.copy(file.toPath(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                addFormDataPart(
                    name = "Logs",
                    filename = tempFile.name,
                    body = tempFile.asRequestBody(tempFile.mimeType?.toMediaTypeOrNull())
                )
            }
            extraFiles
                .mapNotNull { it.validateFile() }
                .forEachIndexed { index, file ->
                    addFormDataPart(
                        name = "extra-$index",
                        filename = file.name,
                        body = file.asRequestBody(file.mimeType?.toMediaTypeOrNull())
                    )
                }
        }

    private suspend fun File.validateFile(): File? = withContext(Dispatchers.IO) {
        if (exists() && length() > 0) this@validateFile else null
    }

    companion object {
        private const val TAG = "ReportRepositoryImpl"
    }
}

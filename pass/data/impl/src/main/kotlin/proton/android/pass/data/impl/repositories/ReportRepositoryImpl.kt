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
import me.proton.core.network.data.ApiProvider
import me.proton.core.report.domain.entity.BugReportMeta
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import proton.android.pass.data.api.repositories.ReportRepository
import proton.android.pass.data.api.usecases.report.Report
import proton.android.pass.data.impl.core.api.CoreApi
import proton.android.pass.log.api.LogFile
import java.io.File
import javax.inject.Inject
import javax.inject.Provider

class ReportRepositoryImpl @Inject constructor(
    private val apiProvider: ApiProvider,
    private val bugReportMetaProvider: Provider<BugReportMeta>,
    @LogFile private val logFile: File?
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
                sendBugReport(
                    getMultipartBodyBuilder(
                        report = report,
                        meta = bugReportMetaProvider.get(),
                        logFile = logFile.takeIf { report.shouldAttachLog },
                        extraFiles = emptyList()
                    ).build()
                )
            }
            .valueOrThrow
    }

    private fun getMultipartBodyBuilder(
        report: Report,
        meta: BugReportMeta,
        logFile: File?,
        extraFiles: List<File>
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
                addFormDataPart(
                    name = "Logs",
                    filename = file.name,
                    body = file.asRequestBody(file.mimeType?.toMediaTypeOrNull())
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

    private fun File.validateFile(): File? = if (exists() && length() > 0) this else null
}

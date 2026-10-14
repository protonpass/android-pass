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


package proton.android.pass.data.impl.fakes

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.Buffer
import proton.android.pass.data.impl.remote.RemoteReportDataSource

class FakeRemoteReportDataSource : RemoteReportDataSource {

    private var result: Result<Unit> = Result.success(Unit)

    private val mutableSentReports = mutableListOf<List<SentPart>>()
    val sentReports: List<List<SentPart>> = mutableSentReports

    fun setResult(value: Result<Unit>) {
        result = value
    }

    override suspend fun sendBugReport(body: RequestBody) {
        val parts = (body as MultipartBody).parts.map { part ->
            val disposition = part.headers?.get("Content-Disposition").orEmpty()
            SentPart(
                name = disposition.attribute("name").orEmpty(),
                filename = disposition.attribute("filename"),
                content = Buffer().also(part.body::writeTo).readUtf8()
            )
        }
        mutableSentReports.add(parts)
        result.getOrThrow()
    }

    private fun String.attribute(key: String): String? = Regex("""\b$key="([^"]*)"""")
        .find(this)
        ?.groupValues
        ?.get(1)

    data class SentPart(val name: String, val filename: String?, val content: String)
}

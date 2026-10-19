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


package proton.android.pass.data.impl.repositories

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.Product
import me.proton.core.report.domain.entity.BugReportMeta
import org.junit.Before
import org.junit.Test
import proton.android.pass.common.fakes.FakeAppDispatchers
import proton.android.pass.data.api.usecases.report.Report
import proton.android.pass.data.fakes.usecases.FakeGetPrimaryUserId
import proton.android.pass.data.impl.fakes.FakeLogExporter
import proton.android.pass.data.impl.fakes.FakeRemoteReportDataSource
import proton.android.pass.domain.UserId
import java.io.IOException
import kotlin.test.assertFailsWith

class ReportRepositoryImplTest {

    private lateinit var remoteReportDataSource: FakeRemoteReportDataSource
    private lateinit var logExporter: FakeLogExporter
    private lateinit var getPrimaryUserId: FakeGetPrimaryUserId
    private lateinit var instance: ReportRepositoryImpl

    @Before
    fun setup() {
        remoteReportDataSource = FakeRemoteReportDataSource()
        logExporter = FakeLogExporter()
        getPrimaryUserId = FakeGetPrimaryUserId().apply { setResult(USER_ID) }
        instance = ReportRepositoryImpl(
            remoteReportDataSource = remoteReportDataSource,
            bugReportMetaProvider = { BUG_REPORT_META },
            logExporter = logExporter,
            getPrimaryUserId = getPrimaryUserId,
            appDispatchers = FakeAppDispatchers()
        )
    }

    @Test
    fun `attaches exported logs to the report`() = runTest {
        instance.sendReport(report(shouldAttachLog = true))

        val exportedFile = logExporter.exportedFiles.single()
        val logsPart = remoteReportDataSource.sentReports.single().single { it.name == LOGS_PART }
        assertThat(logsPart.filename).isEqualTo(exportedFile.name)
        assertThat(logsPart.content).isEqualTo(FakeLogExporter.DEFAULT_CONTENT)
    }

    @Test
    fun `deletes exported log file after sending`() = runTest {
        instance.sendReport(report(shouldAttachLog = true))

        assertThat(logExporter.exportedFiles.single().exists()).isFalse()
    }

    @Test
    fun `deletes exported log file when sending fails`() = runTest {
        remoteReportDataSource.setResult(Result.failure(IOException("network")))

        assertFailsWith<IOException> {
            instance.sendReport(report(shouldAttachLog = true))
        }

        assertThat(logExporter.exportedFiles.single().exists()).isFalse()
    }

    @Test
    fun `sends report without logs when export fails`() = runTest {
        logExporter.setResult(Result.failure(IOException("disk full")))

        instance.sendReport(report(shouldAttachLog = true))

        val parts = remoteReportDataSource.sentReports.single()
        assertThat(parts.map { it.name }).doesNotContain(LOGS_PART)
        assertThat(parts.single { it.name == "Title" }.content).isEqualTo(TITLE)
        assertThat(logExporter.exportedFiles.single().exists()).isFalse()
    }

    @Test
    fun `does not export logs when not requested`() = runTest {
        instance.sendReport(report(shouldAttachLog = false))

        assertThat(logExporter.exportedFiles).isEmpty()
        assertThat(remoteReportDataSource.sentReports.single().map { it.name }).doesNotContain(LOGS_PART)
    }

    private fun report(shouldAttachLog: Boolean) = Report(
        title = TITLE,
        description = "description",
        username = "username",
        email = "user@example.com",
        shouldAttachLog = shouldAttachLog,
        extraFiles = emptySet()
    )

    private companion object {
        private const val LOGS_PART = "Logs"
        private const val TITLE = "title"
        private val USER_ID = UserId("user-id")
        private val BUG_REPORT_META = BugReportMeta(
            appVersionName = "1.0.0",
            clientName = "Android Pass",
            osName = "Android",
            osVersion = "16",
            product = Product.Pass
        )
    }
}

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

import android.content.ContextWrapper
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import proton.android.pass.appconfig.fakes.FakeAppConfig
import proton.android.pass.common.fakes.FakeAppDispatchers
import proton.android.pass.domain.UserId
import java.io.File
import kotlin.io.path.createTempDirectory

class LogExporterImplTest {

    private lateinit var tempDir: File
    private lateinit var logFileManager: LogFileManagerImpl
    private lateinit var exporter: LogExporterImpl
    private lateinit var destination: File

    @Before
    fun setup() {
        tempDir = createTempDirectory("log-exporter-test").toFile()
        val context = TestContext(tempDir)
        val dispatchers = FakeAppDispatchers()
        logFileManager = LogFileManagerImpl(context, dispatchers)
        exporter = LogExporterImpl(
            context = context,
            appConfig = FakeAppConfig(),
            logFileManager = logFileManager,
            appDispatchers = dispatchers
        )
        destination = File(tempDir, "export.log")
    }

    @Test
    fun `export starts with device info header`() = runTest {
        exporter.export(destination, null)

        val lines = destination.readLines()
        assertThat(lines.first()).isEqualTo(SEPARATOR)
        assertThat(lines.drop(1).indexOf(SEPARATOR)).isAtLeast(1)
    }

    @Test
    fun `export merges authenticated and anonymous logs by timestamp`() = runTest {
        writeLog(null, "$T1 I: Tag - anonymous first", "$T3 I: Tag - anonymous second")
        writeLog(USER_ID, "$T2 I: Tag - authenticated")

        exporter.export(destination, USER_ID)

        assertThat(logLines()).containsExactly(
            "$T1 [NA] I: Tag - anonymous first",
            "$T2 [AU] I: Tag - authenticated",
            "$T3 [NA] I: Tag - anonymous second"
        ).inOrder()
    }

    @Test
    fun `export skips authenticated log when userId is null`() = runTest {
        writeLog(null, "$T1 I: Tag - anonymous")
        writeLog(USER_ID, "$T2 I: Tag - authenticated")

        exporter.export(destination, null)

        assertThat(logLines()).containsExactly("$T1 [NA] I: Tag - anonymous")
    }

    @Test
    fun `export collapses consecutive repeated messages`() = runTest {
        writeLog(null, "$T1 I: Tag - same", "$T2 I: Tag - same", "$T3 I: Tag - same")

        exporter.export(destination, null)

        assertThat(logLines()).containsExactly(
            "$T1 [NA] I: Tag - same [repeated 3 times; last at $T3]"
        )
    }

    private suspend fun writeLog(userId: UserId?, vararg lines: String) {
        val file = logFileManager.getLogFile(userId)
        logFileManager.ensureLogFileExists(file)
        file.writeText(lines.joinToString(separator = "\n", postfix = "\n"))
    }

    private fun logLines(): List<String> {
        val lines = destination.readLines()
        val headerEnd = lines.drop(1).indexOf(SEPARATOR) + 1
        return lines.drop(headerEnd + 1).filter(String::isNotEmpty)
    }

    private class TestContext(private val cacheDirectory: File) : ContextWrapper(null) {
        override fun getCacheDir(): File = cacheDirectory
        override fun getPackageName(): String = "proton.android.pass.test"
    }

    private companion object {
        private val USER_ID = UserId("test-user-123")
        private const val SEPARATOR = "-----------------------------------------"
        private const val T1 = "2026-09-23 21:26:38.303"
        private const val T2 = "2026-09-23 21:26:38.305"
        private const val T3 = "2026-09-23 21:26:38.306"
    }
}

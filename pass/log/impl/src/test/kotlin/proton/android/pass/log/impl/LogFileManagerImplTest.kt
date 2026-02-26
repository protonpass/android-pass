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

import android.content.Context
import android.content.ContextWrapper
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Test
import proton.android.pass.common.fakes.FakeAppDispatchers
import java.io.File

class LogFileManagerImplTest {

    private lateinit var tempDir: File
    private lateinit var context: Context
    private lateinit var logFileManager: LogFileManagerImpl

    @Before
    fun setup() {
        tempDir = createTempDir("log-test")
        context = TestContext(tempDir)
        logFileManager = LogFileManagerImpl(context, FakeAppDispatchers())
    }

    @Test
    fun `getLogFile returns user-specific file for logged-in user`() = runTest {
        val userId = UserId("test-user-123")

        val result = logFileManager.getLogFile(userId)

        assertThat(result.path).endsWith("logs/user_test_-123.log")
    }

    @Test
    fun `getLogFile returns unlogged file when userId is null`() = runTest {
        val result = logFileManager.getLogFile(null)

        assertThat(result.path).endsWith("logs/not_authenticated.log")
    }

    @Test
    fun `initializeLogDirectory creates logs directory if not exists`() = runTest {
        val logsDir = File(tempDir, "logs")

        logFileManager.initializeLogDirectory()

        assertThat(logsDir.exists()).isTrue()
        assertThat(logsDir.isDirectory).isTrue()
    }

    @Test
    fun `initializeLogDirectory does not fail if directory already exists`() = runTest {
        val logsDir = File(tempDir, "logs")
        logsDir.mkdirs()

        logFileManager.initializeLogDirectory()

        assertThat(logsDir.exists()).isTrue()
    }

    @Test
    fun `ensureLogFileExists creates file if not exists`() = runTest {
        val userId = UserId("test-user-456")
        val file = logFileManager.getLogFile(userId)

        logFileManager.ensureLogFileExists(file)

        assertThat(file.exists()).isTrue()
    }

    @Test
    fun `getAllUserLogFiles returns all log files`() = runTest {
        val user1 = UserId("abcd-user-1")
        val user2 = UserId("efgh-user-2")
        logFileManager.ensureLogFileExists(logFileManager.getLogFile(user1))
        logFileManager.ensureLogFileExists(logFileManager.getLogFile(user2))
        logFileManager.ensureLogFileExists(logFileManager.getLogFile(null))

        val result = logFileManager.getAllUserLogFiles()

        assertThat(result).hasSize(3)
        assertThat(result.any { it.name == "user_abcd_er-1.log" }).isTrue()
        assertThat(result.any { it.name == "user_efgh_er-2.log" }).isTrue()
        assertThat(result.any { it.name == "not_authenticated.log" }).isTrue()
    }

    @Test
    fun `deleteLogFile removes the specified log file`() = runTest {
        val userId = UserId("to-delete")
        val file = logFileManager.getLogFile(userId)
        logFileManager.ensureLogFileExists(file)

        logFileManager.deleteLogFile(file)

        assertThat(file.exists()).isFalse()
    }

    private class TestContext(private val cacheDirectory: File) : ContextWrapper(null) {
        override fun getCacheDir(): File = cacheDirectory
    }
}

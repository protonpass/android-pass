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

import android.content.Context
import android.content.ContextWrapper
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import proton.android.pass.account.fakes.FakeAccountManager
import org.junit.After
import org.junit.Before
import org.junit.Test
import proton.android.pass.common.fakes.FakeAppDispatchers
import timber.log.Timber
import java.io.File

class FileLoggingTreeTest {

    private lateinit var tempDir: File
    private lateinit var context: Context
    private lateinit var logFileManager: LogFileManagerImpl
    private lateinit var privacySanitizer: PrivacySanitizerImpl
    private lateinit var appDispatchers: FakeAppDispatchers
    private lateinit var accountManager: FakeAccountManager
    private lateinit var fileLoggingTree: FileLoggingTree

    @Before
    fun setup() {
        tempDir = createTempDir("file-logging-tree-test")
        context = TestContext(tempDir)
        appDispatchers = FakeAppDispatchers()
        logFileManager = LogFileManagerImpl(context, appDispatchers)
        privacySanitizer = PrivacySanitizerImpl()
        accountManager = FakeAccountManager()
        fileLoggingTree = FileLoggingTree(
            logFileManager = logFileManager,
            privacySanitizer = privacySanitizer,
            accountManager = accountManager,
            appDispatchers = appDispatchers,
            maxFileSize = 50_000,
            rotationLines = 500
        )
        Timber.plant(fileLoggingTree)
    }

    @After
    fun tearDown() {
        Timber.uproot(fileLoggingTree)
    }

    @Test
    fun `log messages are sanitized before writing`() = runTest {
        val userId = UserId("test-user")
        accountManager.sendPrimaryUserId(userId)
        val file = logFileManager.getLogFile(userId)
        logFileManager.ensureLogFileExists(file)

        Timber.tag("TestTag").i("User email is john.doe@proton.me")
        appDispatchers.testDispatcher.scheduler.advanceUntilIdle()

        val logContent = file.readText()
        assertThat(logContent).contains("[EMAIL_REDACTED]")
        assertThat(logContent).doesNotContain("john.doe@proton.me")
    }

    @Test
    fun `log writes to correct user-specific file`() = runTest {
        val userId = UserId("specific-user")
        accountManager.sendPrimaryUserId(userId)
        val file = logFileManager.getLogFile(userId)
        logFileManager.ensureLogFileExists(file)

        Timber.tag("TestTag").i("Test message")
        appDispatchers.testDispatcher.scheduler.advanceUntilIdle()

        assertThat(file.name).isEqualTo("user_spec_user.log")
        assertThat(file.readText()).contains("Test message")
    }

    @Test
    fun `log does not write messages below INFO priority`() = runTest {
        val userId = UserId("test-user")
        accountManager.sendPrimaryUserId(userId)
        val file = logFileManager.getLogFile(userId)
        logFileManager.ensureLogFileExists(file)

        Timber.tag("TestTag").d("Debug message")
        Timber.tag("TestTag").v("Verbose message")
        appDispatchers.testDispatcher.scheduler.advanceUntilIdle()

        val logContent = file.readText()
        assertThat(logContent).isEmpty()
    }

    @Test
    fun `log writes INFO and above priority messages`() = runTest {
        val userId = UserId("test-user")
        accountManager.sendPrimaryUserId(userId)
        val file = logFileManager.getLogFile(userId)
        logFileManager.ensureLogFileExists(file)

        Timber.tag("TestTag").i("Info message")
        Timber.tag("TestTag").w("Warn message")
        Timber.tag("TestTag").e("Error message")
        appDispatchers.testDispatcher.scheduler.advanceUntilIdle()

        val logContent = file.readText()
        assertThat(logContent).contains("Info message")
        assertThat(logContent).contains("Warn message")
        assertThat(logContent).contains("Error message")
    }

    @Test
    fun `log includes priority character in output`() = runTest {
        val userId = UserId("test-user")
        accountManager.sendPrimaryUserId(userId)
        val file = logFileManager.getLogFile(userId)
        logFileManager.ensureLogFileExists(file)

        Timber.tag("TestTag").i("Test message")
        appDispatchers.testDispatcher.scheduler.advanceUntilIdle()

        val logContent = file.readText()
        assertThat(logContent).contains(" I: ")
    }

    @Test
    fun `log includes tag in output`() = runTest {
        val userId = UserId("test-user")
        accountManager.sendPrimaryUserId(userId)
        val file = logFileManager.getLogFile(userId)
        logFileManager.ensureLogFileExists(file)

        Timber.tag("MyCustomTag").i("Test message")
        appDispatchers.testDispatcher.scheduler.advanceUntilIdle()

        val logContent = file.readText()
        assertThat(logContent).contains("MyCustomTag")
    }

    @Test
    fun `log uses EmptyTag when tag is null`() = runTest {
        val userId = UserId("test-user")
        accountManager.sendPrimaryUserId(userId)
        val file = logFileManager.getLogFile(userId)
        logFileManager.ensureLogFileExists(file)

        Timber.i("Test message")
        appDispatchers.testDispatcher.scheduler.advanceUntilIdle()

        val logContent = file.readText()
        assertThat(logContent).contains("EmptyTag")
    }

    @Test
    fun `log rotates file when size exceeded`() = runTest {
        val userId = UserId("test-user")
        accountManager.sendPrimaryUserId(userId)
        val file = logFileManager.getLogFile(userId)
        logFileManager.ensureLogFileExists(file)

        val largeLine = "x".repeat(10_000)
        for (i in 1..600) {
            file.appendText("Line $i $largeLine\n")
        }

        assertThat(file.length()).isGreaterThan(4 * 1024 * 1024)

        Timber.tag("TestTag").i("Test message")
        appDispatchers.testDispatcher.scheduler.advanceUntilIdle()

        assertThat(file.length()).isLessThan(6 * 1024 * 1024)
        val lineCount = file.readLines().size
        assertThat(lineCount).isAtMost(501)
    }

    @Test
    fun `log rotation keeps last 500 lines`() = runTest {
        val userId = UserId("test-user")
        accountManager.sendPrimaryUserId(userId)
        val file = logFileManager.getLogFile(userId)
        logFileManager.ensureLogFileExists(file)

        val largeLine = "x".repeat(10_000)
        for (i in 1..700) {
            file.appendText("Line $i $largeLine\n")
        }

        assertThat(file.readLines().size).isEqualTo(700)
        assertThat(file.length()).isGreaterThan(4 * 1024 * 1024)

        Timber.tag("TestTag").i("New log entry")
        appDispatchers.testDispatcher.scheduler.advanceUntilIdle()

        val lines = file.readLines()
        assertThat(lines.size).isAtMost(501)
        assertThat(lines.last()).contains("New log entry")
    }

    @Test
    fun `concurrent writes are safe`() = runTest {
        val userId = UserId("test-user")
        accountManager.sendPrimaryUserId(userId)
        val file = logFileManager.getLogFile(userId)
        logFileManager.ensureLogFileExists(file)

        repeat(100) { i ->
            Timber.tag("TestTag").i("Message $i")
        }
        appDispatchers.testDispatcher.scheduler.advanceUntilIdle()

        val lines = file.readLines()
        assertThat(lines).hasSize(100)
    }

    @Test
    fun `log writes to unlogged file when userId is null`() = runTest {
        accountManager.sendPrimaryUserId(null)
        val file = logFileManager.getLogFile(null)
        logFileManager.ensureLogFileExists(file)

        Timber.tag("TestTag").i("Test message")
        appDispatchers.testDispatcher.scheduler.advanceUntilIdle()

        assertThat(file.name).isEqualTo("not_authenticated.log")
        assertThat(file.readText()).contains("Test message")
    }

    @Test
    fun `rotation preserves newest logs on success`() = runTest {
        val userId = UserId("test-user")
        accountManager.sendPrimaryUserId(userId)
        val file = logFileManager.getLogFile(userId)
        logFileManager.ensureLogFileExists(file)

        // Write 700 lines with padding to exceed maxFileSize (50KB)
        val padding = "x".repeat(100)
        for (i in 1..700) {
            file.appendText("Line number $i $padding\n")
        }

        assertThat(file.length()).isGreaterThan(50_000)

        // Trigger rotation
        Timber.tag("TestTag").i("New entry")
        appDispatchers.testDispatcher.scheduler.advanceUntilIdle()

        val lines = file.readLines()
        // Should keep last 500 lines from original + new log
        assertThat(lines.size).isAtMost(501)

        // Verify rotation happened by checking we lost early lines
        val content = file.readText()
        assertThat(content).doesNotContain("Line number 1 ")
        assertThat(content).doesNotContain("Line number 100 ")

        // Verify we kept recent lines
        assertThat(content).contains("Line number 700 ")
        assertThat(content).contains("New entry")
    }

    @Test
    fun `rotation handles extremely large log files without OOM`() = runTest {
        val userId = UserId("test-user")
        accountManager.sendPrimaryUserId(userId)
        val file = logFileManager.getLogFile(userId)
        logFileManager.ensureLogFileExists(file)

        // Create a large log file (simulate many days of logging)
        val largeLine = "x".repeat(5_000)
        for (i in 1..2000) {
            file.appendText("Line $i $largeLine\n")
        }

        // This should trigger rotation without OOM
        Timber.tag("TestTag").i("Rotation trigger")
        appDispatchers.testDispatcher.scheduler.advanceUntilIdle()

        // File should be rotated and reduced
        assertThat(file.exists()).isTrue()
        val lines = file.readLines()
        assertThat(lines.size).isAtMost(501)
    }

    @Test
    fun `rotation is thread-safe with concurrent logging`() = runTest {
        val userId = UserId("test-user")
        accountManager.sendPrimaryUserId(userId)
        val file = logFileManager.getLogFile(userId)
        logFileManager.ensureLogFileExists(file)

        // Pre-fill to near rotation threshold
        val largeLine = "x".repeat(10_000)
        for (i in 1..599) {
            file.appendText("Line $i $largeLine\n")
        }

        // Trigger multiple concurrent logs that will cause rotation
        repeat(50) { i ->
            Timber.tag("TestTag").i("Concurrent message $i")
        }
        appDispatchers.testDispatcher.scheduler.advanceUntilIdle()

        // File should exist and contain the concurrent messages
        assertThat(file.exists()).isTrue()
        val content = file.readText()
        assertThat(content).contains("Concurrent message")
    }

    private class TestContext(private val cacheDirectory: File) : ContextWrapper(null) {
        override fun getCacheDir(): File = cacheDirectory
    }
}

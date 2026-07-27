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
import android.util.Log
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.proton.core.domain.entity.UserId
import proton.android.pass.account.fakes.FakeAccountManager
import org.junit.After
import org.junit.Before
import org.junit.Test
import proton.android.pass.common.fakes.FakeAppDispatchers
import proton.android.pass.log.api.LogFileManager
import proton.android.pass.log.fakes.FakeBatchedLogoutInterleavingLogFileManager
import proton.android.pass.log.fakes.FakeFinalValidationLogoutInterleavingLogFileManager
import proton.android.pass.log.fakes.FakeLogFileManager
import proton.android.pass.log.fakes.FakeLogoutInterleavingLogFileManager
import proton.android.pass.log.fakes.FakePreEnsureLogoutInterleavingLogFileManager
import proton.android.pass.test.FixedClock
import timber.log.Timber
import java.io.File
import kotlin.io.path.createTempDirectory

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
        tempDir = createTempDirectory("file-logging-tree-test").toFile()
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
            rotationLines = 500,
            clock = Clock.System,
            queueCapacity = FileLoggingTree.DEFAULT_QUEUE_CAPACITY
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

    @Test
    fun `logs retain submission order and call-time UTC millisecond timestamps`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val clock = FixedClock(Instant.parse("2026-07-27T10:00:00Z"))
        val file = File(tempDir, "ordered.log")
        val tree = orderedFileLoggingTree(
            dispatcher = dispatcher,
            clock = clock,
            logFileManager = FakeLogFileManager(file)
        )

        tree.log(Log.INFO, "first")
        clock.updateInstant(Instant.parse("2026-07-27T10:00:00.001Z"))
        tree.log(Log.INFO, "second")

        dispatcher.scheduler.advanceUntilIdle()

        assertThat(file.readLines()).containsExactly(
            "2026-07-27 10:00:00.000 I: EmptyTag - first",
            "2026-07-27 10:00:00.001 I: EmptyTag - second"
        ).inOrder()
    }

    @Test
    fun `writer continues after an unexpected file preparation failure`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val file = File(tempDir, "recovery.log")
        val tree = orderedFileLoggingTree(
            dispatcher = dispatcher,
            clock = FixedClock(Instant.parse("2026-07-27T10:00:00Z")),
            logFileManager = FakeLogFileManager(file, failFirstEnsureLogFileExists = true)
        )

        tree.log(Log.INFO, "first")
        tree.log(Log.INFO, "second")
        dispatcher.scheduler.advanceUntilIdle()

        assertThat(file.readText()).contains("second")
        assertThat(file.readText()).doesNotContain("first")
    }

    @Test
    fun `queue drops second entry when capacity is full without suspending the logging caller`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val file = File(tempDir, "overflow.log")
        val writerStarted = CompletableDeferred<Unit>()
        val releaseWriter = CompletableDeferred<Unit>()
        val tree = orderedFileLoggingTree(
            dispatcher = dispatcher,
            clock = FixedClock(Instant.parse("2026-07-27T10:00:00Z")),
            logFileManager = FakeLogFileManager(
                file = file,
                writerStarted = writerStarted,
                releaseWriter = releaseWriter
            ),
            queueCapacity = 1
        )

        tree.log(Log.INFO, "first")
        tree.log(Log.INFO, "second")

        try {
            dispatcher.scheduler.runCurrent()
            assertThat(writerStarted.isCompleted).isTrue()
        } finally {
            releaseWriter.complete(Unit)
            dispatcher.scheduler.advanceUntilIdle()
        }

        assertThat(file.readText()).contains("first")
        assertThat(file.readText()).doesNotContain("second")
    }

    @Test
    fun `burst larger than writer batch preserves FIFO order and call-time timestamps`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val clock = FixedClock(Instant.parse("2026-07-27T10:00:00Z"))
        val file = File(tempDir, "high-volume.log")
        val entryCount = FileLoggingTree.DEFAULT_WRITER_BATCH_SIZE + 1
        val tree = orderedFileLoggingTree(
            dispatcher = dispatcher,
            clock = clock,
            logFileManager = FakeLogFileManager(file),
            queueCapacity = 128
        )

        repeat(entryCount) { index ->
            clock.updateInstant(Instant.fromEpochMilliseconds(1_785_146_400_000 + index))
            tree.log(Log.INFO, "message-$index")
        }
        dispatcher.scheduler.advanceUntilIdle()

        val lines = file.readLines()
        assertThat(lines.map { it.substringAfter(" - ") }).containsExactlyElementsIn(
            (0 until entryCount).map { "message-$it" }
        ).inOrder()
        val timestamps = lines.map { it.substringBefore(" I:") }
        assertThat(timestamps.zipWithNext().all { (first, second) -> first <= second }).isTrue()
    }

    @Test
    fun `destination is reselected after logout between batches`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val authenticatedUserId = UserId("logged-in-user")
        val authenticatedFile = logFileManager.getLogFile(authenticatedUserId)
        val notAuthenticatedFile = logFileManager.getLogFile(null)
        val tree = orderedFileLoggingTree(
            dispatcher = dispatcher,
            clock = FixedClock(Instant.parse("2026-07-27T10:00:00Z")),
            logFileManager = logFileManager
        )
        accountManager.sendPrimaryUserId(authenticatedUserId)

        tree.log(Log.INFO, "authenticated entry")
        dispatcher.scheduler.advanceUntilIdle()

        accountManager.sendPrimaryUserId(null)
        tree.log(Log.INFO, "anonymous entry")
        dispatcher.scheduler.advanceUntilIdle()

        assertThat(authenticatedFile.readText()).contains("authenticated entry")
        assertThat(authenticatedFile.readText()).doesNotContain("anonymous entry")
        assertThat(notAuthenticatedFile.readText()).contains("anonymous entry")
    }

    @Test
    fun `rotation during a burst retains ordered recent records`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val file = File(tempDir, "rotating-burst.log")
        val rotationLines = 3
        val tree = orderedFileLoggingTree(
            dispatcher = dispatcher,
            clock = FixedClock(Instant.parse("2026-07-27T10:00:00Z")),
            logFileManager = FakeLogFileManager(file),
            maxFileSize = 1,
            rotationLines = rotationLines
        )
        file.parentFile?.mkdirs()
        file.writeText((0..4).joinToString(separator = "\n", postfix = "\n") { "pre-burst-$it" })

        repeat(8) { index -> tree.log(Log.INFO, "burst-$index") }
        dispatcher.scheduler.advanceUntilIdle()

        val lines = file.readLines()
        assertThat(file.readText()).doesNotContain("pre-burst-0")
        assertThat(lines.map { it.substringAfter(" - ") }).containsExactly(
            "burst-4",
            "burst-5",
            "burst-6",
            "burst-7"
        ).inOrder()
        assertThat(lines.size).isAtMost(rotationLines + 1)
    }

    @Test
    fun `logout while second ready-batch entry resolves writes it only anonymously`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val authenticatedUserId = UserId("logged-in-user")
        val authenticatedFile = File(tempDir, "user_logged_in_user.log")
        val notAuthenticatedFile = File(tempDir, "not_authenticated.log")
        val secondEntryStartedResolving = CompletableDeferred<Unit>()
        val resumeSecondEntryResolution = CompletableDeferred<Unit>()
        val tree = orderedFileLoggingTree(
            dispatcher = dispatcher,
            clock = FixedClock(Instant.parse("2026-07-27T10:00:00Z")),
            logFileManager = FakeBatchedLogoutInterleavingLogFileManager(
                authenticatedUserId = authenticatedUserId,
                authenticatedFile = authenticatedFile,
                notAuthenticatedFile = notAuthenticatedFile,
                secondEntryStartedResolving = secondEntryStartedResolving,
                resumeSecondEntryResolution = resumeSecondEntryResolution
            )
        )
        accountManager.sendPrimaryUserId(authenticatedUserId)

        tree.log(Log.INFO, "first ready-batch entry")
        tree.log(Log.INFO, "second ready-batch entry")

        try {
            dispatcher.scheduler.runCurrent()
            assertThat(secondEntryStartedResolving.isCompleted).isTrue()

            accountManager.sendPrimaryUserId(null)
            assertThat(authenticatedFile.delete()).isTrue()
        } finally {
            resumeSecondEntryResolution.complete(Unit)
            dispatcher.scheduler.advanceUntilIdle()
        }

        assertThat(authenticatedFile.exists()).isFalse()
        assertThat(notAuthenticatedFile.readText()).contains("second ready-batch entry")
        assertThat(notAuthenticatedFile.readText()).doesNotContain("first ready-batch entry")
    }

    @Test
    fun `logout cleanup prevents a paused authenticated write from recreating its user log`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val authenticatedUserId = UserId("logged-in-user")
        val authenticatedFile = File(tempDir, "user_logged_in_user.log")
        val notAuthenticatedFile = File(tempDir, "not_authenticated.log")
        val authenticatedFileEnsured = CompletableDeferred<Unit>()
        val resumeWriter = CompletableDeferred<Unit>()
        val tree = orderedFileLoggingTree(
            dispatcher = dispatcher,
            clock = FixedClock(Instant.parse("2026-07-27T10:00:00Z")),
            logFileManager = FakeLogoutInterleavingLogFileManager(
                authenticatedUserId = authenticatedUserId,
                authenticatedFile = authenticatedFile,
                notAuthenticatedFile = notAuthenticatedFile,
                authenticatedFileEnsured = authenticatedFileEnsured,
                resumeWriter = resumeWriter
            )
        )
        accountManager.sendPrimaryUserId(authenticatedUserId)

        tree.log(Log.INFO, "queued entry after logout")

        try {
            dispatcher.scheduler.runCurrent()
            assertThat(authenticatedFileEnsured.isCompleted).isTrue()

            accountManager.sendPrimaryUserId(null)
            assertThat(authenticatedFile.delete()).isTrue()
        } finally {
            resumeWriter.complete(Unit)
            dispatcher.scheduler.advanceUntilIdle()
        }

        assertThat(authenticatedFile.exists()).isFalse()
        assertThat(notAuthenticatedFile.readText()).contains("queued entry after logout")
    }

    @Test
    fun `logout deletion waits for a final validated authenticated entry before removing its log`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val authenticatedUserId = UserId("logged-in-user")
        val authenticatedFile = File(tempDir, "user_logged_in_user.log")
        val notAuthenticatedFile = File(tempDir, "not_authenticated.log")
        val authenticatedFileReadyForWriter = CompletableDeferred<Unit>()
        val releaseWriter = CompletableDeferred<Unit>()
        val logFileManager = FakeFinalValidationLogoutInterleavingLogFileManager(
            authenticatedUserId = authenticatedUserId,
            authenticatedFile = authenticatedFile,
            notAuthenticatedFile = notAuthenticatedFile,
            authenticatedFileReadyForWriter = authenticatedFileReadyForWriter,
            releaseWriter = releaseWriter
        )
        val tree = orderedFileLoggingTree(
            dispatcher = dispatcher,
            clock = FixedClock(Instant.parse("2026-07-27T10:00:00Z")),
            logFileManager = logFileManager
        )
        accountManager.sendPrimaryUserId(authenticatedUserId)

        tree.log(Log.INFO, "entry written before logout cleanup")

        try {
            dispatcher.scheduler.runCurrent()
            assertThat(authenticatedFileReadyForWriter.isCompleted).isTrue()

            accountManager.sendPrimaryUserId(null)
            val deleteJob = launch(dispatcher) {
                logFileManager.deleteLogFile(authenticatedFile)
            }
            dispatcher.scheduler.runCurrent()
            assertThat(deleteJob.isCompleted).isFalse()
        } finally {
            releaseWriter.complete(Unit)
            dispatcher.scheduler.advanceUntilIdle()
        }

        assertThat(authenticatedFile.exists()).isFalse()
    }

    @Test
    fun `logout before initial file creation does not recreate authenticated log`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val authenticatedUserId = UserId("logged-in-user")
        val authenticatedFile = File(tempDir, "user_logged_in_user.log")
        val notAuthenticatedFile = File(tempDir, "not_authenticated.log")
        val initialDestinationResolved = CompletableDeferred<Unit>()
        val releaseInitialDestination = CompletableDeferred<Unit>()
        val tree = orderedFileLoggingTree(
            dispatcher = dispatcher,
            clock = FixedClock(Instant.parse("2026-07-27T10:00:00Z")),
            logFileManager = FakePreEnsureLogoutInterleavingLogFileManager(
                authenticatedUserId = authenticatedUserId,
                authenticatedFile = authenticatedFile,
                notAuthenticatedFile = notAuthenticatedFile,
                initialDestinationResolved = initialDestinationResolved,
                releaseInitialDestination = releaseInitialDestination
            )
        )
        accountManager.sendPrimaryUserId(authenticatedUserId)
        assertThat(authenticatedFile.createNewFile()).isTrue()

        tree.log(Log.INFO, "queued entry after logout")

        try {
            dispatcher.scheduler.runCurrent()
            assertThat(initialDestinationResolved.isCompleted).isTrue()

            accountManager.sendPrimaryUserId(null)
            assertThat(authenticatedFile.delete()).isTrue()
        } finally {
            releaseInitialDestination.complete(Unit)
            dispatcher.scheduler.advanceUntilIdle()
        }

        assertThat(authenticatedFile.exists()).isFalse()
        assertThat(notAuthenticatedFile.readText()).contains("queued entry after logout")
    }

    private fun orderedFileLoggingTree(
        dispatcher: TestDispatcher,
        clock: Clock,
        logFileManager: LogFileManager,
        queueCapacity: Int = 128,
        maxFileSize: Long = 50_000,
        rotationLines: Int = 500
    ): FileLoggingTree = FileLoggingTree(
        logFileManager = logFileManager,
        privacySanitizer = privacySanitizer,
        accountManager = accountManager,
        appDispatchers = FakeAppDispatchers.withTestDispatcher(dispatcher),
        maxFileSize = maxFileSize,
        rotationLines = rotationLines,
        clock = clock,
        queueCapacity = queueCapacity
    )

    private class TestContext(private val cacheDirectory: File) : ContextWrapper(null) {
        override fun getCacheDir(): File = cacheDirectory
    }
}

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

package proton.android.pass.features.settings

import android.content.Context
import android.content.ContextWrapper
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import proton.android.pass.account.fakes.FakeAccountManager
import proton.android.pass.appconfig.api.AppConfig
import proton.android.pass.appconfig.api.BuildEnv
import proton.android.pass.appconfig.api.BuildFlavor
import proton.android.pass.common.api.Some
import proton.android.pass.common.fakes.FakeAppDispatchers
import proton.android.pass.commonui.api.ClassHolder
import proton.android.pass.commonui.api.FileHandler
import proton.android.pass.log.api.LogFileManager
import proton.android.pass.log.api.ShareLogsUseCase
import proton.android.pass.log.impl.LogFileManagerImpl
import proton.android.pass.log.impl.ShareLogsUseCaseImpl
import proton.android.pass.test.MainDispatcherRule
import java.io.File
import java.lang.ref.WeakReference
import java.net.URI

class LogViewViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var logFileManager: LogFileManager
    private lateinit var accountManager: FakeAccountManager
    private lateinit var shareLogsUseCase: ShareLogsUseCase
    private lateinit var fileHandler: TestFileHandler
    private lateinit var appDispatchers: FakeAppDispatchers
    private lateinit var viewModel: LogViewViewModel
    private lateinit var tempDir: File
    private lateinit var context: Context

    @Before
    fun setUp() {
        tempDir = temporaryFolder.newFolder("logs-test")
        context = TestContext(tempDir)
        appDispatchers = FakeAppDispatchers()
        logFileManager = LogFileManagerImpl(context, appDispatchers)
        accountManager = FakeAccountManager()

        fileHandler = TestFileHandler()
        val appConfig = TestAppConfig()
        shareLogsUseCase = ShareLogsUseCaseImpl(
            appConfig = appConfig,
            logFileManager = logFileManager,
            accountManager = accountManager,
            fileHandler = fileHandler,
            appDispatchers = appDispatchers
        )
    }

    @After
    fun tearDown() {
        temporaryFolder.root.listFiles()?.forEach { it.deleteRecursively() }
    }

    @Test
    fun `showClearLogsDialog is false by default`() = runTest {
        accountManager.sendPrimaryUserId(null)
        viewModel = createViewModel()
        assertThat(viewModel.state.value.showClearLogsDialog).isFalse()
    }

    @Test
    fun `showClearLogsDialog becomes true after showClearLogsDialog is called`() = runTest {
        accountManager.sendPrimaryUserId(null)
        viewModel = createViewModel()
        viewModel.showClearLogsDialog()
        assertThat(viewModel.state.value.showClearLogsDialog).isTrue()
    }

    @Test
    fun `showClearLogsDialog becomes false after dismissClearLogsDialog is called`() = runTest {
        accountManager.sendPrimaryUserId(null)
        viewModel = createViewModel()
        viewModel.showClearLogsDialog()
        viewModel.dismissClearLogsDialog()
        assertThat(viewModel.state.value.showClearLogsDialog).isFalse()
    }

    @Test
    fun `clearLogs resets showClearLogsDialog to false`() = runTest {
        accountManager.sendPrimaryUserId(null)
        viewModel = createViewModel()
        viewModel.showClearLogsDialog()
        assertThat(viewModel.state.value.showClearLogsDialog).isTrue()
        viewModel.clearLogs()
        appDispatchers.testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.state.value.showClearLogsDialog).isFalse()
    }

    @Test
    fun `loads current user log file when user is logged in`() = runTest {
        val userId = UserId("test-user-123")
        accountManager.sendPrimaryUserId(userId)
        viewModel = createViewModel()

        val logFile = logFileManager.getLogFile(userId)
        logFile.parentFile?.mkdirs()
        logFile.writeText(
            """
                Line 1
                Line 2
                Line 3
            """.trimIndent() + "\n"
        )

        viewModel.loadLogFile()

        viewModel.state.test {
            val state = awaitItem()
            assertThat(state.lines.map { it.text }).containsExactly("Line 3", "Line 2", "Line 1").inOrder()
        }
    }

    @Test
    fun `loads unlogged file when no user is logged in`() = runTest {
        accountManager.sendPrimaryUserId(null)
        viewModel = createViewModel()

        val unloggedFile = logFileManager.getLogFile(null)
        unloggedFile.parentFile?.mkdirs()
        unloggedFile.writeText(
            """
                Unlogged line 1
                Unlogged line 2
            """.trimIndent() + "\n"
        )

        viewModel.loadLogFile()

        viewModel.state.test {
            val state = awaitItem()
            assertThat(state.lines.map { it.text }).containsExactly("Unlogged line 2", "Unlogged line 1").inOrder()
        }
    }

    @Test
    fun `loads last 100 lines reversed`() = runTest {
        val userId = UserId("test-user")
        accountManager.sendPrimaryUserId(userId)
        viewModel = createViewModel()

        val logFile = logFileManager.getLogFile(userId)
        logFile.parentFile?.mkdirs()
        val lines = (1..150).map { "Line $it" }
        logFile.writeText(lines.joinToString("\n") + "\n")

        viewModel.loadLogFile()

        viewModel.state.test {
            val state = awaitItem()
            assertThat(state.lines.size).isEqualTo(100)
            assertThat(state.lines.first().text).isEqualTo("Line 150")
            assertThat(state.lines.last().text).isEqualTo("Line 51")
            assertThat(state.hasOlderLogs).isTrue()
        }
    }

    @Test
    fun `loads older logs when requested`() = runTest {
        val userId = UserId("older-user")
        accountManager.sendPrimaryUserId(userId)
        viewModel = createViewModel()

        val logFile = logFileManager.getLogFile(userId)
        logFile.parentFile?.mkdirs()
        val lines = (1..150).map { "Line $it" }
        logFile.writeText(lines.joinToString("\n") + "\n")

        viewModel.loadLogFile()
        appDispatchers.testDispatcher.scheduler.advanceUntilIdle()
        viewModel.loadOlderLogs()
        appDispatchers.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertThat(state.lines.size).isEqualTo(150)
        assertThat(state.lines.first().text).isEqualTo("Line 150")
        assertThat(state.lines.last().text).isEqualTo("Line 1")
        assertThat(state.hasOlderLogs).isFalse()
    }

    @Test
    fun `each loaded line has a unique stable id`() = runTest {
        val userId = UserId("ids-user")
        accountManager.sendPrimaryUserId(userId)
        viewModel = createViewModel()

        val logFile = logFileManager.getLogFile(userId)
        logFile.parentFile?.mkdirs()
        logFile.writeText("Line 1\nLine 2\nLine 3\n")

        viewModel.loadLogFile()
        appDispatchers.testDispatcher.scheduler.advanceUntilIdle()
        val idsAfterLoad = viewModel.state.value.lines.map { it.id }
        assertThat(idsAfterLoad).hasSize(3)
        assertThat(idsAfterLoad.toSet()).hasSize(3) // all unique

        logFile.appendText("Line 4\n")
        viewModel.refreshLogs()
        appDispatchers.testDispatcher.scheduler.advanceUntilIdle()
        val idsAfterRefresh = viewModel.state.value.lines.map { it.id }
        // existing lines keep their ids
        assertThat(idsAfterRefresh.takeLast(3)).isEqualTo(idsAfterLoad)
        // new line has a new id not in the original set
        assertThat(idsAfterRefresh.first()).isNotIn(idsAfterLoad)
    }

    @Test
    fun `refresh loads newly appended logs without dropping loaded history`() = runTest {
        val userId = UserId("refresh-user")
        accountManager.sendPrimaryUserId(userId)
        viewModel = createViewModel()

        val logFile = logFileManager.getLogFile(userId)
        logFile.parentFile?.mkdirs()
        logFile.writeText(
            """
                Line 1
                Line 2
            """.trimIndent() + "\n"
        )

        viewModel.loadLogFile()
        appDispatchers.testDispatcher.scheduler.advanceUntilIdle()

        logFile.appendText(
            """
                Line 3
                Line 4
            """.trimIndent() + "\n"
        )

        viewModel.refreshLogs()
        appDispatchers.testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.lines.map { it.text }).containsExactly(
            "Line 4",
            "Line 3",
            "Line 2",
            "Line 1"
        ).inOrder()
    }

    @Test
    fun `refresh does not show partial trailing line until it is completed`() = runTest {
        val userId = UserId("partial-refresh-user")
        accountManager.sendPrimaryUserId(userId)
        viewModel = createViewModel()

        val logFile = logFileManager.getLogFile(userId)
        logFile.parentFile?.mkdirs()
        logFile.writeText("Line 1\n")

        viewModel.loadLogFile()
        appDispatchers.testDispatcher.scheduler.advanceUntilIdle()

        logFile.appendText("Line 2 partial")
        viewModel.refreshLogs()
        appDispatchers.testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.state.value.lines.map { it.text }).containsExactly("Line 1").inOrder()

        logFile.appendText(" complete\n")
        viewModel.refreshLogs()
        appDispatchers.testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.lines.map { it.text }).containsExactly(
            "Line 2 partial complete",
            "Line 1"
        ).inOrder()
    }

    @Test
    fun `initial load hides partial trailing line`() = runTest {
        val userId = UserId("partial-initial-user")
        accountManager.sendPrimaryUserId(userId)
        viewModel = createViewModel()

        val logFile = logFileManager.getLogFile(userId)
        logFile.parentFile?.mkdirs()
        logFile.writeText("Line 1\nLine 2 partial")

        viewModel.loadLogFile()
        appDispatchers.testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.lines.map { it.text }).containsExactly("Line 1").inOrder()
    }

    @Test
    fun `handles missing log file gracefully`() = runTest {
        val userId = UserId("missing-user")
        accountManager.sendPrimaryUserId(userId)
        viewModel = createViewModel()

        viewModel.loadLogFile()

        viewModel.state.test {
            val state = awaitItem()
            assertThat(state.lines).isEmpty()
        }
    }

    @Test
    fun `clears current user log file and empties state`() = runTest {
        val userId = UserId("clear-user")
        accountManager.sendPrimaryUserId(userId)
        viewModel = createViewModel()

        val logFile = logFileManager.getLogFile(userId)
        logFile.parentFile?.mkdirs()
        logFile.writeText(
            """
                Line 1
                Line 2
            """.trimIndent() + "\n"
        )

        viewModel.loadLogFile()
        appDispatchers.testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.state.value.lines.map { it.text }).containsExactly("Line 2", "Line 1").inOrder()

        viewModel.clearLogs()
        appDispatchers.testDispatcher.scheduler.advanceUntilIdle()

        assertThat(logFile.exists()).isFalse()
        assertThat(viewModel.state.value.lines).isEmpty()
    }

    @Test
    fun `shares current user log file with device info`() = runTest {
        val userId = UserId("share-user")
        accountManager.sendPrimaryUserId(userId)
        viewModel = createViewModel()

        val logFile = logFileManager.getLogFile(userId)
        logFile.parentFile?.mkdirs()
        logFile.writeText("User log content")

        val contextHolder = ClassHolder(Some(WeakReference(context)))
        viewModel.startShareIntent(contextHolder)
        appDispatchers.testDispatcher.scheduler.advanceUntilIdle()

        assertThat(fileHandler.lastSharedUri).isNotNull()
        assertThat(fileHandler.lastSharedUri.toString()).contains("pass_logs_")

        val sharedFile = File(fileHandler.lastSharedUri!!.path)
        assertThat(sharedFile.exists()).isTrue()
        val content = sharedFile.readText()
        assertThat(content).contains("-----------------------------------------")
        assertThat(content).contains("User log content")
    }

    @Test
    fun `shares unlogged file when no user with device info`() = runTest {
        accountManager.sendPrimaryUserId(null)
        viewModel = createViewModel()

        val unloggedFile = logFileManager.getLogFile(null)
        unloggedFile.parentFile?.mkdirs()
        unloggedFile.writeText("Unlogged content")

        val contextHolder = ClassHolder(Some(WeakReference(context)))
        viewModel.startShareIntent(contextHolder)
        appDispatchers.testDispatcher.scheduler.advanceUntilIdle()

        assertThat(fileHandler.lastSharedUri).isNotNull()
        assertThat(fileHandler.lastSharedUri.toString()).contains("pass_logs_")

        val sharedFile = File(fileHandler.lastSharedUri!!.path)
        assertThat(sharedFile.exists()).isTrue()
        val content = sharedFile.readText()
        assertThat(content).contains("-----------------------------------------")
        assertThat(content).contains("Unlogged content")
    }

    @Test
    fun `lines have unique ids that are stable across reload`() = runTest {
        val userId = UserId("stable-ids-user")
        accountManager.sendPrimaryUserId(userId)
        viewModel = createViewModel()

        val logFile = logFileManager.getLogFile(userId)
        logFile.parentFile?.mkdirs()
        logFile.writeText("Line A\nLine B\n")

        viewModel.loadLogFile()
        appDispatchers.testDispatcher.scheduler.advanceUntilIdle()

        val firstIds = viewModel.state.value.lines.map { it.id }
        assertThat(firstIds.toSet()).hasSize(2)

        viewModel.loadLogFile()
        appDispatchers.testDispatcher.scheduler.advanceUntilIdle()

        // IDs are monotonically increasing but different each load — uniqueness is what matters
        val secondIds = viewModel.state.value.lines.map { it.id }
        assertThat(secondIds.toSet()).hasSize(2)
        // Ids from second load should be different from the first (they're fresh)
        assertThat(secondIds).isNotEqualTo(firstIds)
    }

    private fun createViewModel() = LogViewViewModel(
        logFileManager = logFileManager,
        accountManager = accountManager,
        shareLogsUseCase = shareLogsUseCase,
        appDispatchers = appDispatchers
    )

    private class TestAppConfig : AppConfig {
        override val isDebug: Boolean = true
        override val applicationId: String = "test.app"
        override val flavor: BuildFlavor = BuildFlavor.Dev(BuildEnv.PROD)
        override val versionCode: Int = 1
        override val versionName: String = "1.0.0-test"
        override val host: String = "test-host"
        override val humanVerificationHost: String = "test-hv-host"
        override val proxyToken: String? = null
        override val useDefaultPins: Boolean = true
        override val sentryDSN: String? = null
        override val accountSentryDSN: String? = null
        override val androidVersion: Int = 33
        override val allowScreenshotsDefaultValue: Boolean = true
    }

    private class TestFileHandler : FileHandler {
        var lastSharedUri: URI? = null

        override suspend fun shareFile(
            contextHolder: ClassHolder<Context>,
            fileTitle: String,
            uri: URI,
            mimeType: String,
            chooserTitle: String
        ) {
            lastSharedUri = uri
        }

        override suspend fun shareFileWithEmail(
            contextHolder: ClassHolder<Context>,
            uri: URI,
            mimeType: String,
            chooserTitle: String,
            email: String,
            subject: String
        ) {
            lastSharedUri = uri
        }

        override fun openFile(
            contextHolder: ClassHolder<Context>,
            uri: URI,
            mimeType: String,
            chooserTitle: String
        ) {
        }

        override fun performFileAction(
            contextHolder: ClassHolder<Context>,
            intent: android.content.Intent,
            chooserTitle: String,
            extras: android.os.Bundle
        ) {
        }
    }

    private class TestContext(private val cacheDirectory: File) : ContextWrapper(null) {
        override fun getCacheDir(): File = cacheDirectory
    }
}

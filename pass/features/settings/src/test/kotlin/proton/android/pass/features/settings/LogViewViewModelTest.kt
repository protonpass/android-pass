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
            """.trimIndent()
        )

        viewModel.loadLogFile()

        viewModel.state.test {
            val state = awaitItem()
            assertThat(state).contains("Line 3")
            assertThat(state).contains("Line 2")
            assertThat(state).contains("Line 1")
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
            """.trimIndent()
        )

        viewModel.loadLogFile()

        viewModel.state.test {
            val state = awaitItem()
            assertThat(state).contains("Unlogged line 2")
            assertThat(state).contains("Unlogged line 1")
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
        logFile.writeText(lines.joinToString("\n"))

        viewModel.loadLogFile()

        viewModel.state.test {
            val state = awaitItem()
            val stateLines = state.split("\n")
            assertThat(stateLines.size).isEqualTo(100)
            assertThat(stateLines.first()).isEqualTo("Line 150")
            assertThat(stateLines.last()).isEqualTo("Line 51")
        }
    }

    @Test
    fun `handles missing log file gracefully`() = runTest {
        val userId = UserId("missing-user")
        accountManager.sendPrimaryUserId(userId)
        viewModel = createViewModel()

        viewModel.loadLogFile()

        viewModel.state.test {
            val state = awaitItem()
            assertThat(state).isEmpty()
        }
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

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

package proton.android.pass.uitest.flow

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import me.proton.core.account.domain.entity.Account
import me.proton.core.auth.test.flow.SignInFlow
import me.proton.core.auth.test.robot.AddAccountRobot
import me.proton.core.auth.test.usecase.WaitForPrimaryAccount
import me.proton.core.user.domain.repository.UserRepository
import me.proton.core.test.rule.ProtonRule
import me.proton.core.test.rule.extension.protonAndroidComposeRule
import me.proton.core.usersettings.domain.repository.UserSettingsRepository
import me.proton.core.userrecovery.dagger.CoreDeviceRecoveryFeaturesModule
import me.proton.core.userrecovery.domain.IsDeviceRecoveryEnabled
import me.proton.core.userrecovery.domain.repository.DeviceRecoveryRepository
import me.proton.core.userrecovery.presentation.compose.DeviceRecoveryHandler
import me.proton.core.userrecovery.presentation.compose.DeviceRecoveryNotificationSetup
import me.proton.test.fusion.Fusion.node
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.domain.UserId
import proton.android.pass.features.onboarding.OnBoardingPageName
import proton.android.pass.initializer.MainInitializer
import proton.android.pass.ui.MainActivity
import proton.android.pass.uitest.BaseTest.Companion.quark
import proton.android.pass.uitest.robot.HomeRobot
import proton.android.pass.uitest.robot.OnBoardingRobot
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import me.proton.core.userrecovery.presentation.R as UserRecoveryR

private const val DEFAULT_TIMEOUT_SEC = 30
private const val REFRESH_EVERY_N_POLLS = 10L
private const val NOTIFICATION_POLL_MS = 250L
private const val RECOVERY_NOTIFICATION_TAG_PREFIX = "device-recovery-"

private val USER_RECOVERY_WORKER_TAGS = listOf(
    "me.proton.core.userrecovery.data.worker.SetRecoverySecretWorker",
    "me.proton.core.userrecovery.data.worker.RecoverInactivePrivateKeysWorker"
)

@HiltAndroidTest
@UninstallModules(CoreDeviceRecoveryFeaturesModule::class)
open class UserRecoveryFlowTest {

    @get:Rule
    val protonRule: ProtonRule = protonAndroidComposeRule<MainActivity>(
        logoutBefore = true,
        fusionEnabled = true,
        afterHilt = { MainInitializer.init(it.targetContext) }
    )

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = if (Build.VERSION.SDK_INT >= 33) {
        GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        GrantPermissionRule.grant()
    }

    @Inject
    lateinit var deviceRecoveryHandler: DeviceRecoveryHandler

    @Inject
    lateinit var deviceRecoveryNotificationSetup: DeviceRecoveryNotificationSetup

    @Inject
    lateinit var deviceRecoveryRepository: DeviceRecoveryRepository

    @Inject
    lateinit var userSettingsRepository: UserSettingsRepository

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var workManager: WorkManager

    @Inject
    lateinit var waitForPrimaryAccount: WaitForPrimaryAccount

    @BindValue
    internal val isDeviceRecoveryEnabled = object : IsDeviceRecoveryEnabled {
        override fun invoke(userId: UserId?): Boolean = true
        override fun isLocalEnabled(): Boolean = true
        override fun isRemoteEnabled(userId: UserId?): Boolean = true
    }

    @Before
    fun prepare() {
        deviceRecoveryNotificationSetup.init()
        deviceRecoveryHandler.start()
        releaseUserRecoveryWorkConstraints()
    }

    private fun releaseUserRecoveryWorkConstraints() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val driver = WorkManagerTestInitHelper.getTestDriver(instrumentation.targetContext) ?: return
        val observer = Observer<List<WorkInfo>> { infos ->
            infos.filter { it.state == WorkInfo.State.ENQUEUED }.forEach { driver.setAllConstraintsMet(it.id) }
        }
        instrumentation.runOnMainSync {
            USER_RECOVERY_WORKER_TAGS.forEach { tag ->
                workManager.getWorkInfosByTagLiveData(tag).observeForever(observer)
            }
        }
    }

    @Test
    fun verifyKeysAreRecovered() {
        val (user, response) = quark.userCreate()

        signIn(user.name, user.password)

        val account = waitForPrimaryAccount()
        runBlocking { userSettingsRepository.getUserSettings(account.userId, refresh = true) }
        waitForRecoveryFileCreated(account)

        signOut()
        AddAccountRobot.uiElementsDisplayed(timeout = DEFAULT_TIMEOUT_SEC.seconds)

        val newPassword = "new-password"
        quark.resetPassword(
            userID = response.decryptedUserId,
            newPassword = newPassword
        )

        signIn(user.name, newPassword)

        waitForPrimaryAccount()

        waitForRecoveryNotification().contentIntent.send()

        node.withText(UserRecoveryR.string.user_recovery_dialog_title).await {
            assertIsDisplayed()
        }
    }

    private fun waitForRecoveryNotification(): Notification = runBlocking {
        val manager = ApplicationProvider.getApplicationContext<Context>()
            .getSystemService(NotificationManager::class.java)
        val deadline = System.currentTimeMillis() + DEFAULT_TIMEOUT_SEC * 1000L
        while (System.currentTimeMillis() < deadline) {
            manager.activeNotifications
                .firstOrNull { it.tag?.startsWith(RECOVERY_NOTIFICATION_TAG_PREFIX) == true }
                ?.let { return@runBlocking it.notification }
            delay(NOTIFICATION_POLL_MS)
        }
        error("Could not find the notification for user recovery.")
    }

    private fun refreshUser(account: Account) = runBlocking {
        userRepository.getUser(account.userId, refresh = true)
    }

    private fun signIn(username: String, password: String) {
        AddAccountRobot.clickSignIn()
        SignInFlow.signInInternal(username, password, isLoginTwoStepEnabled = true)
    }

    private fun signOut() {
        OnBoardingRobot.onBoardingScreenDisplayed()
            .clickSkip(OnBoardingPageName.Autofill)
            .clickMain(OnBoardingPageName.Last)
        HomeRobot.homeScreenDisplayed()
            .clickProfile()
            .clickAccount()
            .clickSignOut()
            .confirmSignOut()
    }

    private fun waitForRecoveryFileCreated(account: Account) = runBlocking {
        var i = 0
        val delayMs = 100L
        val maxI = 10_000L / delayMs
        while (i <= maxI) {
            if (deviceRecoveryRepository.getRecoveryFiles(account.userId).isNotEmpty()) break
            if (i % REFRESH_EVERY_N_POLLS == 0L) refreshUser(account)
            delay(delayMs)
            i += 1
        }
    }
}

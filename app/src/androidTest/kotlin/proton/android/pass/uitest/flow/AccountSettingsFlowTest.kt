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

import androidx.test.espresso.Espresso
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.test.robot.AccountSettingsRobot
import me.proton.core.auth.test.flow.SignInFlow
import me.proton.core.auth.test.robot.AddAccountRobot
import me.proton.core.eventmanager.domain.EventManagerConfig
import me.proton.core.eventmanager.domain.EventManagerProvider
import me.proton.core.test.rule.ProtonRule
import me.proton.core.test.rule.annotation.PrepareUser
import me.proton.core.test.rule.annotation.TestUserData
import me.proton.core.test.rule.extension.protonAndroidComposeRule
import me.proton.core.usersettings.test.flow.PasswordManagementFlow
import me.proton.core.usersettings.test.flow.UpdateRecoveryEmailFlow
import me.proton.core.util.kotlin.random
import org.junit.Rule
import org.junit.Test
import proton.android.pass.features.onboarding.OnBoardingPageName
import proton.android.pass.initializer.MainInitializer
import proton.android.pass.ui.MainActivity
import proton.android.pass.uitest.TestUsers
import proton.android.pass.uitest.robot.HomeRobot
import proton.android.pass.uitest.robot.OnBoardingRobot
import proton.android.pass.uitest.robot.SyncDialogRobot
import javax.inject.Inject

@HiltAndroidTest
open class AccountSettingsFlowTest {

    @get:Rule
    val protonRule: ProtonRule = protonAndroidComposeRule<MainActivity>(
        logoutBefore = true,
        fusionEnabled = true,
        afterHilt = { MainInitializer.init(it.targetContext) }
    )

    @Inject
    lateinit var accountManager: AccountManager

    @Inject
    lateinit var eventManagerProvider: EventManagerProvider

    private val testUser: TestUserData
        get() = protonRule.testDataRule.mainTestUser ?: error("No User data was seeded")

    private fun signIn(name: String = testUser.name, password: String = testUser.password) {
        AddAccountRobot.clickSignIn()
        SignInFlow.signInInternal(name, password, isLoginTwoStepEnabled = true)
        SyncDialogRobot.completeSyncIfDisplayed()
    }

    private fun refreshUserEvents() = runBlocking {
        val userId = accountManager.getPrimaryUserId().filterNotNull().first()
        eventManagerProvider.get(EventManagerConfig.Core(userId)).process()
    }

    private fun startAccountSettings(): AccountSettingsRobot {
        OnBoardingRobot.onBoardingScreenDisplayed()
            .clickSkip(OnBoardingPageName.Autofill)
            .clickMain(OnBoardingPageName.Last)
        SyncDialogRobot.completeSyncIfDisplayed()
        return HomeRobot
            .clickProfile()
            .clickAccount()
            .coreAccountSettings()
    }

    private fun startPasswordManagement() {
        startAccountSettings().clickPasswordManagement()
    }

    private fun startRecoveryEmail() {
        startAccountSettings().clickRecoveryEmail()
    }

    @Test
    @PrepareUser
    fun changePasswordSuccess() {
        val new = "new${String.random()}"

        signIn()
        startPasswordManagement()

        PasswordManagementFlow
            .changeLoginPassword(current = testUser.password, new = new, confirm = new)
            .successPasswordUpdatedIsDisplayed()
    }

    @Test
    fun changePasswordErrorDoNotMatch() {
        val new = "new${String.random()}"
        val other = "other${String.random()}"

        signIn(TestUsers.free.name, TestUsers.free.password)
        startPasswordManagement()

        PasswordManagementFlow
            .changeLoginPassword(current = TestUsers.free.password, new = new, confirm = other)
            .errorPasswordDoNotMatchIsDisplayed()
    }

    @Test
    @PrepareUser
    fun changeRecoveryEmailSuccess() {
        val new = "new.${testUser.name}@email.com"

        signIn()
        startRecoveryEmail()

        UpdateRecoveryEmailFlow
            .changeRecoveryEmail(new = new, confirm = new, password = testUser.password)
            .successEmailUpdatedIsDisplayed()
    }

    @Test
    fun changeRecoveryEmailErrorDoNotMatch() {
        val new = "new.${TestUsers.free.name}@email.com"
        val other = "other.${TestUsers.free.name}@email.com"

        signIn(TestUsers.free.name, TestUsers.free.password)
        startRecoveryEmail()

        UpdateRecoveryEmailFlow
            .changeRecoveryEmail(new = new, confirm = other, password = null)
            .errorEmailDoNotMatchIsDisplayed()
    }

    @Test
    @PrepareUser
    fun startAndCancelAccountRecovery() {
        signIn()
        startPasswordManagement()
        Espresso.closeSoftKeyboard()

        PasswordManagementFlow.startRecovery()
        refreshUserEvents()
        PasswordManagementFlow.cancelRecovery(password = testUser.password)
    }
}

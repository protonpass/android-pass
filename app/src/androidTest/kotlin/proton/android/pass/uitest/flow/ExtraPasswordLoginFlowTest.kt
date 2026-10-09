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
import me.proton.core.auth.domain.feature.IsLoginTwoStepEnabled
import me.proton.core.auth.domain.testing.LoginTestHelper
import me.proton.core.auth.test.flow.SignInFlow
import me.proton.core.auth.test.robot.AddAccountRobot
import me.proton.core.auth.test.robot.login.LoginTwoStepRobot
import me.proton.core.compose.component.PROTON_OUTLINED_TEXT_INPUT_TAG
import me.proton.core.test.quark.data.Plan
import me.proton.core.test.rule.ProtonRule
import me.proton.core.test.rule.annotation.PrepareUser
import me.proton.core.test.rule.annotation.TestUserData
import me.proton.core.test.rule.annotation.payments.TestSubscriptionData
import me.proton.core.test.rule.extension.protonAndroidComposeRule
import me.proton.test.fusion.Fusion.node
import org.junit.Rule
import org.junit.Test
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.extrapassword.SetupExtraPassword
import proton.android.pass.features.onboarding.OnBoardingPageName
import proton.android.pass.initializer.MainInitializer
import proton.android.pass.ui.MainActivity
import proton.android.pass.uitest.robot.ExtraPasswordRobot
import proton.android.pass.uitest.robot.HomeRobot
import proton.android.pass.uitest.robot.OnBoardingRobot
import proton.android.pass.uitest.robot.ProfileRobot
import proton.android.pass.uitest.robot.SyncDialogRobot
import proton.android.pass.uitest.rule.ExtraPasswordSetupRule
import javax.inject.Inject

@HiltAndroidTest
class ExtraPasswordLoginFlowTest {

    @Inject
    lateinit var loginTestHelper: LoginTestHelper

    @Inject
    lateinit var setupExtraPassword: SetupExtraPassword

    @Inject
    lateinit var encryptionContextProvider: EncryptionContextProvider

    @Inject
    lateinit var isLoginTwoStepEnabled: IsLoginTwoStepEnabled

    private val extraPasswordUser: TestUserData
        get() = preparedUser(EXTRA_PASSWORD_USER_TAG)

    private val regularUser: TestUserData
        get() = preparedUser(REGULAR_USER_TAG)

    @get:Rule
    val protonRule: ProtonRule = protonAndroidComposeRule<MainActivity>(
        logoutBefore = true,
        fusionEnabled = true,
        afterHilt = { MainInitializer.init(it.targetContext) },
        additionalRules = linkedSetOf(
            ExtraPasswordSetupRule(
                username = { extraPasswordUser.name },
                password = { extraPasswordUser.password },
                extraPassword = EXTRA_PASSWORD,
                loginTestHelper = { loginTestHelper },
                setupExtraPassword = { setupExtraPassword },
                encryptionContextProvider = { encryptionContextProvider }
            )
        )
    )

    @Test
    @PrepareUser(withTag = EXTRA_PASSWORD_USER_TAG)
    fun loginWithCorrectExtraPasswordReachesHome() {
        AddAccountRobot.clickSignIn()
        signIn(extraPasswordUser)

        ExtraPasswordRobot
            .extraPasswordScreenDisplayed()
            .openedInMainActivityTask()
            .typeExtraPassword(EXTRA_PASSWORD)
            .clickUnlock()

        skipOnBoardingToHome()
    }

    @Test
    @PrepareUser(withTag = EXTRA_PASSWORD_USER_TAG)
    fun loginWithWrongExtraPasswordShowsErrorAndAllowsRetry() {
        AddAccountRobot.clickSignIn()
        signIn(extraPasswordUser)

        ExtraPasswordRobot
            .extraPasswordScreenDisplayed()
            .openedInMainActivityTask()
            .typeExtraPassword(WRONG_EXTRA_PASSWORD)
            .clickUnlock()
            .incorrectPasswordErrorDisplayed()
            .extraPasswordScreenDisplayed()
            .typeExtraPassword(EXTRA_PASSWORD)
            .clickUnlock()

        skipOnBoardingToHome()
    }

    @Test
    @PrepareUser(withTag = REGULAR_USER_TAG)
    @PrepareUser(
        withTag = EXTRA_PASSWORD_USER_TAG,
        subscriptionData = TestSubscriptionData(plan = Plan.PassPlus)
    )
    fun addSecondAccountWithExtraPasswordBecomesActiveAccount() {
        AddAccountRobot.clickSignIn()
        signIn(regularUser)
        skipOnBoardingToHome()

        HomeRobot
            .clickProfile()
            .openAccountSwitcher(regularUser.email)
            .clickAddAccount()

        signIn(extraPasswordUser)

        ExtraPasswordRobot
            .extraPasswordScreenDisplayed()
            .openedInMainActivityTask()
            .typeExtraPassword(EXTRA_PASSWORD)
            .clickUnlock()

        SyncDialogRobot.completeSyncIfDisplayed()
        ProfileRobot
            .profileScreenDisplayed()
            .accountDisplayed(extraPasswordUser.email)
            .openAccountSwitcher(extraPasswordUser.email)
            .accountDisplayed(regularUser.email)
    }

    @Test
    @PrepareUser(withTag = EXTRA_PASSWORD_USER_TAG)
    fun backFromExtraPasswordCancelsLoginAndLetsUserSignInAgain() {
        AddAccountRobot.clickSignIn()
        signIn(extraPasswordUser)
        ExtraPasswordRobot
            .extraPasswordScreenDisplayed()
            .openedInMainActivityTask()
            .hideKeyboard()

        Espresso.pressBackUnconditionally()

        AddAccountRobot.clickSignIn()
        signIn(extraPasswordUser)
        ExtraPasswordRobot
            .extraPasswordScreenDisplayed()
            .openedInMainActivityTask()
            .typeExtraPassword(EXTRA_PASSWORD)
            .clickUnlock()

        skipOnBoardingToHome()
    }

    @Test
    @PrepareUser(withTag = REGULAR_USER_TAG)
    @PrepareUser(
        withTag = EXTRA_PASSWORD_USER_TAG,
        subscriptionData = TestSubscriptionData(plan = Plan.PassPlus)
    )
    fun backFromExtraPasswordCancelsAddAccountAndAllowsRetry() {
        AddAccountRobot.clickSignIn()
        signIn(regularUser)
        skipOnBoardingToHome()

        HomeRobot
            .clickProfile()
            .openAccountSwitcher(regularUser.email)
            .clickAddAccount()

        signIn(extraPasswordUser)
        ExtraPasswordRobot
            .extraPasswordScreenDisplayed()
            .openedInMainActivityTask()
            .hideKeyboard()

        Espresso.pressBackUnconditionally()

        node.withTag(PROTON_OUTLINED_TEXT_INPUT_TAG).replaceText(extraPasswordUser.password)
        LoginTwoStepRobot.clickContinue()
        ExtraPasswordRobot
            .extraPasswordScreenDisplayed()
            .openedInMainActivityTask()
            .typeExtraPassword(EXTRA_PASSWORD)
            .clickUnlock()

        SyncDialogRobot.completeSyncIfDisplayed()
        ProfileRobot
            .profileScreenDisplayed()
            .accountDisplayed(extraPasswordUser.email)
    }

    private fun preparedUser(tag: String): TestUserData = protonRule.testDataRule.preparedUsers[tag]
        ?: error("No user was seeded with tag $tag")

    private fun signIn(user: TestUserData) {
        SignInFlow.signInInternal(
            username = user.name,
            password = user.password,
            isLoginTwoStepEnabled = isLoginTwoStepEnabled()
        )
    }

    private fun skipOnBoardingToHome() {
        OnBoardingRobot
            .onBoardingScreenDisplayed()
            .clickSkip(OnBoardingPageName.Autofill)
            .clickMain(OnBoardingPageName.Last)
        SyncDialogRobot.completeSyncIfDisplayed()
        HomeRobot.homeScreenDisplayed()
    }

    private companion object {
        const val REGULAR_USER_TAG = "main"
        const val EXTRA_PASSWORD_USER_TAG = "extraPassword"
        const val EXTRA_PASSWORD = "extra-Passw0rd-uitest"
        const val WRONG_EXTRA_PASSWORD = "wrong-Passw0rd-uitest"
    }
}

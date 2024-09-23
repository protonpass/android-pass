package proton.android.pass.uitest.flow

import dagger.hilt.android.testing.HiltAndroidTest
import me.proton.core.accountmanager.test.robot.AccountSettingsRobot
import me.proton.core.test.rule.ProtonRule
import me.proton.core.test.rule.extension.protonAndroidComposeRule
import me.proton.core.usersettings.test.MinimalUserSettingsTest
import org.junit.Rule
import proton.android.pass.features.onboarding.OnBoardingPageName
import proton.android.pass.ui.MainActivity
import proton.android.pass.uitest.robot.HomeRobot
import proton.android.pass.uitest.robot.OnBoardingRobot

@HiltAndroidTest
open class AccountSettingsFlowTest : MinimalUserSettingsTest {

    // TODO: rework and fix account tests - CP-8721.

    @get:Rule
    override val protonRule: ProtonRule = protonAndroidComposeRule<MainActivity>(
        logoutBefore = true
    )

    private fun startAccountSettings(): AccountSettingsRobot {
        OnBoardingRobot.onBoardingScreenDisplayed()
            .clickSkip(OnBoardingPageName.Autofill)
            .clickMain(OnBoardingPageName.Last)
        return HomeRobot
            .clickProfile()
            .clickAccount()
            .coreAccountSettings()
    }

    override fun startPasswordManagement() {
        startAccountSettings().clickPasswordManagement()
    }

    override fun startRecoveryEmail() {
        startAccountSettings().clickRecoveryEmail()
    }
}

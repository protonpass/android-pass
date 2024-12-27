package proton.android.pass.uitest.flow

import dagger.hilt.android.testing.HiltAndroidTest
import me.proton.core.accountmanager.test.robot.AccountSettingsRobot
import me.proton.core.test.quark.Quark
import me.proton.core.usersettings.test.MinimalUserSettingsTest
import org.junit.Before
import proton.android.pass.features.onboarding.OnBoardingPageName
import proton.android.pass.uitest.BaseTest
import proton.android.pass.uitest.robot.HomeRobot
import proton.android.pass.uitest.robot.OnBoardingRobot

@HiltAndroidTest
class AccountSettingsFlowTest : BaseTest(), MinimalUserSettingsTest {

    override val quark: Quark = BaseTest.quark

    @Before
    fun preventHumanVerification() {
        quark.jailUnban()
    }

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

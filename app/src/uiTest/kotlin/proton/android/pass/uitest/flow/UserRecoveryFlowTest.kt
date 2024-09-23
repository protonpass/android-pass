package proton.android.pass.uitest.flow

import android.Manifest
import android.os.Build
import androidx.test.rule.GrantPermissionRule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import me.proton.core.auth.test.usecase.WaitForPrimaryAccount
import me.proton.core.domain.entity.UserId
import me.proton.core.test.quark.Quark
import me.proton.core.userrecovery.dagger.CoreDeviceRecoveryFeaturesModule
import me.proton.core.userrecovery.domain.IsDeviceRecoveryEnabled
import me.proton.core.userrecovery.domain.repository.DeviceRecoveryRepository
import me.proton.core.userrecovery.presentation.compose.DeviceRecoveryHandler
import me.proton.core.userrecovery.presentation.compose.DeviceRecoveryNotificationSetup
import me.proton.core.userrecovery.test.MinimalUserRecoveryTest
import org.junit.Rule
import proton.android.pass.features.onboarding.OnBoardingPageName
import proton.android.pass.uitest.BaseTest
import proton.android.pass.uitest.robot.HomeRobot
import proton.android.pass.uitest.robot.OnBoardingRobot
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(CoreDeviceRecoveryFeaturesModule::class)
open class UserRecoveryFlowTest : BaseTest(), MinimalUserRecoveryTest {

    // TODO: rework and fix account tests. Migrate to ProtonRule - CP-8721.

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = if (Build.VERSION.SDK_INT >= 33) {
        GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        GrantPermissionRule.grant()
    }

    override val quark: Quark = BaseTest.quark

    @Inject
    override lateinit var deviceRecoveryHandler: DeviceRecoveryHandler

    @Inject
    override lateinit var deviceRecoveryNotificationSetup: DeviceRecoveryNotificationSetup

    @Inject
    override lateinit var deviceRecoveryRepository: DeviceRecoveryRepository

    @Inject
    override lateinit var waitForPrimaryAccount: WaitForPrimaryAccount

    @BindValue
    internal val isDeviceRecoveryEnabled = object : IsDeviceRecoveryEnabled {
        override fun invoke(userId: UserId?): Boolean = true
        override fun isLocalEnabled(): Boolean = true
        override fun isRemoteEnabled(userId: UserId?): Boolean = true
    }

    override fun signOut() {
        OnBoardingRobot.onBoardingScreenDisplayed()
            .clickSkip(OnBoardingPageName.Autofill)
            .clickMain(OnBoardingPageName.Last)
        HomeRobot.homeScreenDisplayed()
            .clickProfile()
            .clickAccount()
            .clickSignOut()
            .confirmSignOut()
    }
}

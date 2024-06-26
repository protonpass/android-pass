package proton.android.pass.uitest.flow

import android.Manifest
import android.os.Build
import androidx.test.rule.GrantPermissionRule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import me.proton.core.accountmanager.data.AccountStateHandler
import me.proton.core.accountrecovery.dagger.CoreAccountRecoveryFeaturesModule
import me.proton.core.accountrecovery.domain.IsAccountRecoveryEnabled
import me.proton.core.accountrecovery.test.MinimalAccountRecoveryNotificationTest
import me.proton.core.auth.test.usecase.WaitForPrimaryAccount
import me.proton.core.domain.entity.UserId
import me.proton.core.eventmanager.domain.EventManagerProvider
import me.proton.core.eventmanager.domain.repository.EventMetadataRepository
import me.proton.core.network.data.ApiProvider
import me.proton.core.notification.dagger.CoreNotificationFeaturesModule
import me.proton.core.notification.domain.repository.NotificationRepository
import me.proton.core.notification.domain.usecase.IsNotificationsEnabled
import me.proton.core.test.quark.Quark
import org.junit.Rule
import proton.android.pass.uitest.BaseTest
import proton.android.pass.uitest.robot.OnBoardingRobot
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(
    CoreAccountRecoveryFeaturesModule::class,
    CoreNotificationFeaturesModule::class,
)
class AccountRecoveryFlowTest : BaseTest(), MinimalAccountRecoveryNotificationTest {
    @get:Rule
    val grantPermissionRule: GrantPermissionRule = if (Build.VERSION.SDK_INT >= 33) {
        GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        GrantPermissionRule.grant()
    }

    @BindValue
    internal val isAccountRecoveryEnabled = object : IsAccountRecoveryEnabled {
        override fun invoke(userId: UserId?): Boolean = true
    }

    @BindValue
    internal val isNotificationsEnabled = IsNotificationsEnabled { true }

    @Inject
    override lateinit var accountStateHandler: AccountStateHandler

    @Inject
    override lateinit var apiProvider: ApiProvider

    @Inject
    override lateinit var eventManagerProvider: EventManagerProvider

    @Inject
    override lateinit var eventMetadataRepository: EventMetadataRepository

    @Inject
    override lateinit var notificationRepository: NotificationRepository

    @Inject
    override lateinit var waitForPrimaryAccount: WaitForPrimaryAccount

    override val quark: Quark = BaseTest.quark

    override fun verifyAfterLogin() {
        OnBoardingRobot.onBoardingScreenDisplayed()
    }
}

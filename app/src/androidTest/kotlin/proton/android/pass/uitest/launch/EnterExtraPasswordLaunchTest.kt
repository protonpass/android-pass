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

package proton.android.pass.uitest.launch

import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import me.proton.core.auth.dagger.CoreAuthFeaturesModule
import me.proton.core.auth.domain.feature.IsCommonPasswordCheckEnabled
import me.proton.core.auth.domain.feature.IsCredentialLessEnabled
import me.proton.core.auth.domain.feature.IsFido2Enabled
import me.proton.core.auth.domain.feature.IsLoginTwoStepEnabled
import me.proton.core.auth.domain.feature.IsSsoCustomTabEnabled
import me.proton.core.auth.domain.feature.IsSsoEnabled
import me.proton.core.auth.presentation.ui.LoginTwoStepActivity
import me.proton.core.auth.test.robot.AddAccountRobot
import me.proton.core.auth.test.fake.FakeIsCommonPasswordCheckEnabled
import me.proton.core.auth.test.fake.FakeIsCredentialLessEnabled
import me.proton.core.auth.test.fake.FakeIsFido2Enabled
import me.proton.core.auth.test.fake.FakeIsLoginTwoStepEnabled
import me.proton.core.auth.test.fake.FakeIsSsoCustomTabEnabled
import me.proton.core.auth.test.fake.FakeIsSsoEnabled
import org.junit.Test
import proton.android.pass.domain.UserId
import proton.android.pass.uitest.robot.ActivityStackRobot
import proton.android.pass.uitest.robot.ExtraPasswordRobot

@HiltAndroidTest
@UninstallModules(CoreAuthFeaturesModule::class)
class EnterExtraPasswordLaunchTest : EnterExtraPasswordLaunchBaseTest() {

    @BindValue
    internal val isLoginTwoStepEnabled: IsLoginTwoStepEnabled = FakeIsLoginTwoStepEnabled(enabled = true)

    @BindValue
    internal val isSsoEnabled: IsSsoEnabled = FakeIsSsoEnabled(enabled = false)

    @BindValue
    internal val isSsoCustomTabEnabled: IsSsoCustomTabEnabled = FakeIsSsoCustomTabEnabled(enabled = false)

    @BindValue
    internal val isCredentialLessEnabled: IsCredentialLessEnabled = FakeIsCredentialLessEnabled()

    @BindValue
    internal val isCommonPasswordCheckEnabled: IsCommonPasswordCheckEnabled = FakeIsCommonPasswordCheckEnabled()

    @BindValue
    internal val isFido2Enabled: IsFido2Enabled = FakeIsFido2Enabled()

    @Test
    fun opensInMainActivityTaskWhenLoginTwoStepIsOnTop() {
        AddAccountRobot.clickSignIn()
        ActivityStackRobot.awaitResumed(LoginTwoStepActivity::class.java)

        enterExtraPasswordLauncher.start(UserId(USER_ID))

        ExtraPasswordRobot.openedInMainActivityTask()
    }

    private companion object {
        const val USER_ID = "extra-password-launch-test"
    }
}

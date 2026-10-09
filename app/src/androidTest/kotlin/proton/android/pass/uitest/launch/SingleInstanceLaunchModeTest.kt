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

import android.app.Activity
import android.content.ComponentName
import android.content.pm.ActivityInfo
import androidx.test.platform.app.InstrumentationRegistry
import me.proton.core.auth.presentation.ui.AddAccountActivity
import me.proton.core.auth.presentation.ui.LoginSsoActivity
import me.proton.core.auth.presentation.ui.LoginTwoStepActivity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import proton.android.pass.isSingleInstance
import proton.android.pass.launchModeOf

class SingleInstanceLaunchModeTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun loginSsoActivityIsSingleInstance() {
        val launchMode = context.packageManager.launchModeOf(ComponentName(context, LoginSsoActivity::class.java))
        assertTrue(
            "launchMode=${launchMode.getOrNull()} error=${launchMode.exceptionOrNull()}",
            launchMode.getOrNull() == ActivityInfo.LAUNCH_SINGLE_INSTANCE
        )
    }

    @Test
    fun loginTwoStepActivityIsSingleInstance() {
        assertTrue(isSingleInstance(LoginTwoStepActivity::class.java))
    }

    @Test
    fun addAccountActivityIsNotSingleInstance() {
        assertFalse(isSingleInstance(AddAccountActivity::class.java))
    }

    private fun isSingleInstance(activityClass: Class<out Activity>): Boolean =
        context.packageManager.isSingleInstance(ComponentName(context, activityClass))
}

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

package proton.android.pass.uitest.robot

import androidx.test.espresso.Espresso
import me.proton.test.fusion.Fusion.node
import proton.android.pass.enterextrapassword.EnterExtraPasswordActivity
import proton.android.pass.features.auth.R
import proton.android.pass.ui.MainActivity
import kotlin.time.Duration.Companion.seconds

object ExtraPasswordRobot : Robot {

    private val screenTimeout = 60.seconds

    private val passwordLabel get() = node.withText(R.string.auth_extra_password_label)
    private val passwordInput get() = node.isSetText()
    private val unlockButton get() = node.withText(R.string.auth_unlock_button)
    private val incorrectPasswordError get() = node.withText(R.string.auth_error_wrong_password_no_attempts)

    fun extraPasswordScreenDisplayed(): ExtraPasswordRobot = apply {
        passwordLabel.await(screenTimeout) { assertIsDisplayed() }
    }

    fun typeExtraPassword(extraPassword: String): ExtraPasswordRobot = apply {
        passwordInput.await { assertIsDisplayed() }
        passwordInput.replaceText(extraPassword)
    }

    fun hideKeyboard(): ExtraPasswordRobot = apply {
        passwordInput.await { assertIsDisplayed() }
        Espresso.closeSoftKeyboard()
    }

    fun clickUnlock(): ExtraPasswordRobot = apply {
        unlockButton.await { assertIsDisplayed() }
        unlockButton.click()
    }

    fun incorrectPasswordErrorDisplayed(): ExtraPasswordRobot = apply {
        incorrectPasswordError.await(screenTimeout) { assertIsDisplayed() }
    }

    fun openedInMainActivityTask(): ExtraPasswordRobot = apply {
        ActivityStackRobot
            .awaitResumed(EnterExtraPasswordActivity::class.java)
            .assertSameTask(expected = MainActivity::class.java, actual = EnterExtraPasswordActivity::class.java)
    }
}

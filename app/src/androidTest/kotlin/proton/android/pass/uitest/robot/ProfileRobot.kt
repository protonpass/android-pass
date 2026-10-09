/*
 * Copyright (c) 2026 Proton AG.
 * This file is part of Proton Pass.
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

import me.proton.test.fusion.Fusion.node
import proton.android.pass.features.profile.ProfileScreenTestTag
import proton.android.pass.features.profile.R
import proton.android.pass.features.profile.accountswitcher.AccountSwitcherTestTag

object ProfileRobot : Robot {

    private val profileScreen get() = node.withTag(ProfileScreenTestTag.SCREEN)

    private val accountSwitcher get() = node.withTag(AccountSwitcherTestTag.COLLAPSED_ROW)

    private val accountOptions get() = node.withTag(AccountSwitcherTestTag.ROW_OPTIONS)

    private val manageAccount get() = node.withText(R.string.account_switcher_manage_account)

    private val addAccount get() = node.withText(R.string.account_switcher_add_account)

    private fun account(email: String) = node.withText(email)

    fun profileScreenDisplayed(): ProfileRobot = apply {
        profileScreen.await { assertIsDisplayed() }
    }

    fun clickAccount(): AccountRobot {
        accountSwitcher.await { assertIsDisplayed() }
        accountSwitcher.click()
        accountOptions.await { assertIsDisplayed() }
        accountOptions.click()
        manageAccount.await { assertIsDisplayed() }
        manageAccount.click()
        return AccountRobot
    }

    fun accountDisplayed(email: String): ProfileRobot = apply {
        account(email).await { assertIsDisplayed() }
    }

    fun openAccountSwitcher(primaryEmail: String): ProfileRobot = apply {
        account(primaryEmail).await { assertIsDisplayed() }
        account(primaryEmail).click()
    }

    fun clickAddAccount() {
        addAccount.await { assertIsDisplayed() }
        addAccount.click()
    }
}

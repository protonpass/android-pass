/*
 * Copyright (c) 2023 Proton AG.
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
import proton.android.pass.featureprofile.impl.AccountProfileSectionTestTag
import proton.android.pass.featureprofile.impl.ProfileScreenTestTag

object ProfileRobot : Robot {

    private val profileScreen get() = node.withTag(ProfileScreenTestTag.screen)

    private val accountSetting get() = node.withTag(AccountProfileSectionTestTag.accountSetting)

    fun profileScreenDisplayed(): ProfileRobot = apply {
        profileScreen.await { assertIsDisplayed() }
    }

    fun clickAccount(): AccountRobot {
        accountSetting.await { assertIsDisplayed() }
        accountSetting.click()
        return AccountRobot
    }
}

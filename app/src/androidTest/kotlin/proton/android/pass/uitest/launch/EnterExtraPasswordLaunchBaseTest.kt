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

import me.proton.core.test.rule.ProtonRule
import me.proton.core.test.rule.extension.protonAndroidComposeRule
import org.junit.Rule
import proton.android.pass.enterextrapassword.EnterExtraPasswordLauncher
import proton.android.pass.initializer.MainInitializer
import proton.android.pass.ui.MainActivity
import javax.inject.Inject

abstract class EnterExtraPasswordLaunchBaseTest {

    @Inject
    lateinit var enterExtraPasswordLauncher: EnterExtraPasswordLauncher

    @get:Rule
    val protonRule: ProtonRule = protonAndroidComposeRule<MainActivity>(
        logoutBefore = true,
        fusionEnabled = true,
        afterHilt = { MainInitializer.init(it.targetContext) }
    )
}

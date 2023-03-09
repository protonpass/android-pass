/*
 * Copyright (c) 2023 Proton AG.
 * This file is part of Proton Drive.
 *
 * Proton Drive is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Drive is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Drive.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.uitest

import android.content.Context
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import me.proton.core.auth.domain.testing.LoginTestHelper
import me.proton.core.test.quark.Quark
import me.proton.core.test.quark.data.User
import me.proton.test.fusion.FusionConfig
import org.junit.Rule
import proton.android.pass.BuildConfig
import proton.android.pass.ui.MainActivity
import proton.android.pass.uitest.robot.Robot
import proton.android.pass.uitest.rule.HiltInjectRule
import proton.android.pass.uitest.rule.LogoutAllRule
import proton.android.pass.uitest.rule.MainInitializerRule
import javax.inject.Inject

open class BaseTest {

    @get:Rule(order = RuleOrder_00_First)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = RuleOrder_10_Initialization)
    val mainInitializerRule = MainInitializerRule()

    @get:Rule(order = RuleOrder_20_Injection)
    val hiltInjectRule = HiltInjectRule(hiltRule)

    @get:Rule(order = RuleOrder_21_Injected)
    val logoutAllRule = LogoutAllRule { loginTestHelper }

    @get:Rule(order = RuleOrder_30_ActivityLaunch)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var loginTestHelper: LoginTestHelper

    init {
        FusionConfig.Compose.testRule.set(composeTestRule)
        FusionConfig.Compose.useUnmergedTree.set(true)
        FusionConfig.Compose.onFailure = { /* screenshot() */ }
    }

    inline fun <T : Robot> T.verify(crossinline block: T.() -> Any): T = apply { block() }

    companion object {
        const val RuleOrder_00_First = 0
        const val RuleOrder_10_Initialization = 10
        const val RuleOrder_11_Initialized = 11
        const val RuleOrder_20_Injection = 20
        const val RuleOrder_21_Injected = 21
        const val RuleOrder_29_ActivityPreLaunch = 29
        const val RuleOrder_30_ActivityLaunch = 30
        const val RuleOrder_31_ActivityPostLaunch = 31
        const val RuleOrder_99_Last = 99

        private val context: Context
            get() = InstrumentationRegistry.getInstrumentation().context

        val users = User.Users.fromJson(
            json = context.assets.open("users.json").bufferedReader().use { it.readText() }
        )
        val quark = Quark.fromJson(
            json = context.assets.open("internal_api.json").bufferedReader().use { it.readText() },
            host = BuildConfig.HOST,
            proxyToken = BuildConfig.PROXY_TOKEN
        )
    }
}

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

package proton.android.pass.uitest

import androidx.test.platform.app.InstrumentationRegistry
import me.proton.core.test.quark.data.Plan
import me.proton.core.test.quark.data.User
import org.json.JSONObject

object TestUsers {

    private val config: JSONObject by lazy {
        InstrumentationRegistry.getInstrumentation().context.assets
            .open("test_users.json")
            .bufferedReader()
            .use { JSONObject(it.readText()) }
    }

    private val usernamePrefix: String get() = config.getString("usernamePrefix")
    private val emailDomain: String get() = config.getString("emailDomain")
    private val password: String get() = config.getString("password")
    private val totpSecret: String get() = config.getString("totpSecret")

    val free: User by lazy { user(scenario = "free") }
    val plus: User by lazy { user(scenario = "plus", plan = Plan.PassPlus) }
    val unlimited: User by lazy { user(scenario = "unlimited", plan = Plan.Unlimited) }
    val twoPassword: User by lazy { user(scenario = "twopassword", passphrase = password) }
    val totp: User by lazy { user(scenario = "totp", twoFa = totpSecret) }
    val recovery: User by lazy { user(scenario = "recovery") }
    val populated: User by lazy { user(scenario = "populated") }

    private fun user(
        scenario: String,
        plan: Plan = Plan.Free,
        passphrase: String = "",
        twoFa: String = ""
    ): User {
        val name = "$usernamePrefix$scenario"
        return User(
            name = name,
            password = password,
            email = "$name@$emailDomain",
            plan = plan,
            passphrase = passphrase,
            twoFa = twoFa
        )
    }
}

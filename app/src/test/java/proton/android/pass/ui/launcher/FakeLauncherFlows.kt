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

package proton.android.pass.ui.launcher

import androidx.activity.ComponentActivity
import kotlinx.coroutines.CoroutineScope
import proton.android.pass.domain.UserId

internal class FakeLauncherFlows : LauncherFlows {

    val calls = mutableListOf<String>()

    override fun register(activity: ComponentActivity, scope: CoroutineScope) {
        calls.add("register")
    }

    override fun startAddAccount() {
        calls.add("addAccount")
    }

    override fun startSignup() {
        calls.add("signup")
    }

    override fun startLogin(username: String?) {
        calls.add("login:$username")
    }

    override fun startPasswordManagement(userId: UserId) {
        calls.add("passwordManagement:${userId.id}")
    }

    override fun startRecoveryEmail(userId: UserId) {
        calls.add("recoveryEmail:${userId.id}")
    }

    override fun startSecurityKeys(userId: UserId) {
        calls.add("securityKeys:${userId.id}")
    }
}

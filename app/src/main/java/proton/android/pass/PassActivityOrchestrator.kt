/*
 * Copyright (c) 2024 Proton AG
 * This file is part of Proton AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

class PassActivityOrchestrator @Inject constructor() {

    private var enterExtraPasswordLauncher: ActivityResultLauncher<UserId>? = null

    private fun registerEnterExtraPasswordResult(caller: ActivityResultCaller): ActivityResultLauncher<UserId> =
        caller.registerForActivityResult(StartExtraPassword) { /* Unused */ }

    private fun <T> checkRegistered(launcher: ActivityResultLauncher<T>?) =
        checkNotNull(launcher) { "You must call passOrchestrator.register(context) before starting workflow!" }

    /**
     * Register all needed workflow for internal usage.
     *
     * Note: This function have to be called [ComponentActivity.onCreate]] before [ComponentActivity.onResume].
     */
    fun register(caller: ActivityResultCaller) {
        enterExtraPasswordLauncher = registerEnterExtraPasswordResult(caller)
    }

    /**
     * Unregister all workflow activity launcher and listener.
     */
    fun unregister() {
        enterExtraPasswordLauncher?.unregister()
        enterExtraPasswordLauncher = null
    }

    fun startEnterExtraPassword(userId: UserId) {
        checkRegistered(enterExtraPasswordLauncher).launch(userId)
    }
}

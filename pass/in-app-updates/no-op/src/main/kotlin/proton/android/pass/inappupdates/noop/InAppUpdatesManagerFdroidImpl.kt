/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.inappupdates.noop

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import proton.android.pass.inappupdates.api.InAppUpdateState
import proton.android.pass.inappupdates.api.InAppUpdatesManager
import javax.inject.Inject

class InAppUpdatesManagerFdroidImpl @Inject constructor() : InAppUpdatesManager {
    override fun checkForUpdates(launcher: ActivityResultLauncher<IntentSenderRequest>) {

    }

    override fun completeUpdate() {

    }

    override fun checkUpdateStalled() {

    }

    override fun observeInAppUpdateState(): Flow<InAppUpdateState> = flowOf(InAppUpdateState.Idle)

    override fun declineUpdate() {

    }

    override fun tearDown() {

    }

}

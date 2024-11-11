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

package proton.android.pass.data.impl.work

import androidx.work.WorkManager
import proton.android.pass.data.api.work.WorkerItem
import proton.android.pass.data.api.work.WorkerLauncher
import javax.inject.Inject

class WorkerLauncherImpl @Inject constructor(
    private val workManager: WorkManager
) : WorkerLauncher {
    override fun launch(workerItem: WorkerItem) {
        when (workerItem) {
            is WorkerItem.SingleItemAssetLink -> {
                val request = SingleItemAssetLinkWorker.getRequestFor(workerItem.websites)
                workManager.enqueue(request)
            }

            is WorkerItem.MarkInAppMessageAsDismissed -> {
                val request = MarkInAppMessageAsDismissedWorker.getRequestFor(
                    userId = workerItem.userId,
                    inAppMessageId = workerItem.inAppMessageId
                )
                workManager.enqueue(request)
            }
        }
    }
}

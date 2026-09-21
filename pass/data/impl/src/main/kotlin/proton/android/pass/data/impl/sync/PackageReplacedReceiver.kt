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

package proton.android.pass.data.impl.sync

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

/**
 * Enqueues [FolderRepairWorker] when the app is updated, so the repair runs even if the user never
 * opens Pass.
 */
@AndroidEntryPoint
class PackageReplacedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var workManager: WorkManager

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != Intent.ACTION_MY_PACKAGE_REPLACED) return

        PassLogger.i(TAG, "Package replaced, enqueueing folder repair")
        workManager.enqueueUniqueWork(
            FolderRepairWorker.WORKER_UNIQUE_NAME,
            FolderRepairWorker.EXISTING_WORK_POLICY,
            FolderRepairWorker.getRequest()
        )
    }

    private companion object {

        private const val TAG = "PackageReplacedReceiver"

    }
}

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


package proton.android.pass.data.impl.fakes

import android.app.PendingIntent
import androidx.lifecycle.LiveData
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.Operation
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkContinuation
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import androidx.work.WorkRequest
import com.google.common.util.concurrent.ListenableFuture
import java.util.UUID

class FakeWorkManager : WorkManager() {

    val enqueuedUniqueWorkNames = mutableListOf<String>()

    override fun getConfiguration(): Configuration = unused()
    override fun enqueue(requests: MutableList<out WorkRequest>): Operation = unused()
    override fun beginWith(work: MutableList<OneTimeWorkRequest>): WorkContinuation = unused()
    override fun beginUniqueWork(
        uniqueWorkName: String,
        existingWorkPolicy: ExistingWorkPolicy,
        work: MutableList<OneTimeWorkRequest>
    ): WorkContinuation = unused()
    override fun enqueueUniqueWork(
        uniqueWorkName: String,
        existingWorkPolicy: ExistingWorkPolicy,
        work: MutableList<OneTimeWorkRequest>
    ): Operation {
        enqueuedUniqueWorkNames.add(uniqueWorkName)
        return object : Operation {
            override fun getState(): LiveData<Operation.State> = unused()
            override fun getResult(): ListenableFuture<Operation.State.SUCCESS> = unused()
        }
    }
    override fun enqueueUniquePeriodicWork(
        uniqueWorkName: String,
        existingPeriodicWorkPolicy: ExistingPeriodicWorkPolicy,
        periodicWork: PeriodicWorkRequest
    ): Operation = unused()
    override fun cancelWorkById(id: UUID): Operation = unused()
    override fun cancelAllWorkByTag(tag: String): Operation = unused()
    override fun cancelUniqueWork(uniqueWorkName: String): Operation = unused()
    override fun cancelAllWork(): Operation = unused()
    override fun createCancelPendingIntent(id: UUID): PendingIntent = unused()
    override fun pruneWork(): Operation = unused()
    override fun getLastCancelAllTimeMillisLiveData(): LiveData<Long> = unused()
    override fun getLastCancelAllTimeMillis(): ListenableFuture<Long> = unused()
    override fun getWorkInfoByIdLiveData(id: UUID): LiveData<WorkInfo> = unused()
    override fun getWorkInfoById(id: UUID): ListenableFuture<WorkInfo> = unused()
    override fun getWorkInfosByTagLiveData(tag: String): LiveData<MutableList<WorkInfo>> = unused()
    override fun getWorkInfosByTag(tag: String): ListenableFuture<MutableList<WorkInfo>> = unused()
    override fun getWorkInfosForUniqueWorkLiveData(uniqueWorkName: String): LiveData<MutableList<WorkInfo>> = unused()
    override fun getWorkInfosForUniqueWork(uniqueWorkName: String): ListenableFuture<MutableList<WorkInfo>> = unused()
    override fun getWorkInfosLiveData(workQuery: WorkQuery): LiveData<MutableList<WorkInfo>> = unused()
    override fun getWorkInfos(workQuery: WorkQuery): ListenableFuture<MutableList<WorkInfo>> = unused()
    override fun updateWork(request: WorkRequest): ListenableFuture<UpdateResult> = unused()

    private fun unused(): Nothing = error("FakeWorkManager does not support this call")
}

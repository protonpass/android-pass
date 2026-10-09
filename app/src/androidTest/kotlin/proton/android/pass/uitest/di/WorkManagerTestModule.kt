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

package proton.android.pass.uitest.di

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import proton.android.pass.di.WorkManagerModule
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [WorkManagerModule::class]
)
object WorkManagerTestModule {

    @Provides
    @Singleton
    fun provideWorkManager(
        @ApplicationContext context: Context,
        hiltWorkerFactory: HiltWorkerFactory
    ): WorkManager {
        val config = Configuration.Builder()
            .setWorkerFactory(hiltWorkerFactory)
            .setExecutor(SynchronousExecutor())
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
        val workManager = WorkManager.getInstance(context)
        val testDriver = requireNotNull(WorkManagerTestInitHelper.getTestDriver(context))
        val enqueuedWork = WorkQuery.Builder
            .fromStates(listOf(WorkInfo.State.ENQUEUED))
            .build()
        Handler(Looper.getMainLooper()).post {
            workManager.getWorkInfosLiveData(enqueuedWork).observeForever { workInfos ->
                workInfos.forEach { workInfo ->
                    runCatching { testDriver.setAllConstraintsMet(workInfo.id) }
                }
            }
        }
        return workManager
    }
}

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

package proton.android.pass.initializer

import android.content.Context
import androidx.lifecycle.coroutineScope
import androidx.startup.Initializer
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.commonui.api.PassAppLifecycleProvider
import proton.android.pass.data.api.repositories.AttachmentRepository
import proton.android.pass.data.api.repositories.ItemSyncStatus
import proton.android.pass.data.api.repositories.ItemSyncStatusRepository
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.ObserveReadyAccounts
import proton.android.pass.data.api.usecases.attachments.AttachmentDownloadScheduler
import proton.android.pass.domain.UserId
import proton.android.pass.domain.attachments.AttachmentDownloadStatus
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value

class AttachmentDownloadInitializer : Initializer<Unit> {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Suppress("LongMethod")
    override fun create(context: Context) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            AttachmentDownloadInitializerEntryPoint::class.java
        )

        val lifecycleProvider = entryPoint.passAppLifecycleProvider()
        val preferencesRepository = entryPoint.userPreferencesRepository()
        val observeReadyAccounts = entryPoint.observeReadyAccounts()
        val shareRepository = entryPoint.shareRepository()
        val attachmentRepository = entryPoint.attachmentRepository()
        val downloadScheduler = entryPoint.attachmentDownloadScheduler()
        val syncStatusRepository = entryPoint.itemSyncStatusRepository()

        // Observe: when toggle is ON and there are pending downloads, schedule the workers
        observeReadyAccounts()
            .flatMapLatest { accounts ->
                if (accounts.isEmpty()) return@flatMapLatest flowOf(emptySet<UserId>())
                val flows = accounts.map { account ->
                    val userId = account.userId
                    preferencesRepository.observeDownloadAllAttachmentsPref(userId)
                        .map { it.value() }
                        .distinctUntilChanged()
                        .flatMapLatest { isEnabled ->
                            if (!isEnabled) return@flatMapLatest flowOf(false)
                            shareRepository.observeOfflineEnabledShareIds(userId)
                                .flatMapLatest { shareIds ->
                                    if (shareIds.isEmpty()) return@flatMapLatest flowOf(false)
                                    attachmentRepository
                                        .observePendingDownloads(userId, shareIds, includeFailed = false)
                                        .map { pendingList ->
                                            pendingList.any {
                                                it.downloadStatus == AttachmentDownloadStatus.Pending
                                            }
                                        }
                                        .distinctUntilChanged()
                                }
                        }
                        .map { hasPendingDownloads -> userId.takeIf { hasPendingDownloads } }
                }
                combine(flows) { results -> results.filterNotNull().toSet() }
            }
            .distinctUntilChanged()
            .onEach { userIdsWithPendingDownloads ->
                userIdsWithPendingDownloads.forEach { userId ->
                    PassLogger.i(TAG, "Pending downloads detected, scheduling workers")
                    safeRunCatching { downloadScheduler.scheduleAll(userId) }
                        .onFailure { error ->
                            PassLogger.w(TAG, "Error scheduling downloads")
                            PassLogger.w(TAG, error)
                        }
                }
            }
            .launchIn(lifecycleProvider.lifecycle.coroutineScope)

        // After each successful sync, schedule downloads so the worker
        // can refresh attachment metadata for newly synced items
        lifecycleProvider.lifecycle.coroutineScope.launch {
            syncStatusRepository.observeSyncStatus().collectLatest { status ->
                if (status is ItemSyncStatus.SyncSuccess) {
                    observeReadyAccounts()
                        .first()
                        .forEach { account ->
                            val isEnabled = preferencesRepository
                                .observeDownloadAllAttachmentsPref(account.userId)
                                .first()
                                .value()
                            if (!isEnabled) return@forEach
                            safeRunCatching { downloadScheduler.scheduleAll(account.userId) }
                                .onFailure { error ->
                                    PassLogger.w(TAG, "Error scheduling downloads after sync")
                                    PassLogger.w(TAG, error)
                                }
                        }
                }
            }
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>?>> = emptyList()

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AttachmentDownloadInitializerEntryPoint {
        fun passAppLifecycleProvider(): PassAppLifecycleProvider
        fun userPreferencesRepository(): UserPreferencesRepository
        fun observeReadyAccounts(): ObserveReadyAccounts
        fun shareRepository(): ShareRepository
        fun attachmentRepository(): AttachmentRepository
        fun attachmentDownloadScheduler(): AttachmentDownloadScheduler
        fun itemSyncStatusRepository(): ItemSyncStatusRepository
    }

    companion object {
        private const val TAG = "AttachmentDownloadInitializer"
    }
}

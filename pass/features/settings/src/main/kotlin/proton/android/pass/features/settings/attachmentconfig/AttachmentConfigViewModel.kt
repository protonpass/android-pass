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

package proton.android.pass.features.settings.attachmentconfig

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.android.pass.data.api.usecases.attachments.AttachmentDownloadScheduler
import proton.android.pass.data.api.usecases.attachments.GetVaultUsage
import proton.android.pass.data.api.usecases.attachments.SetSharedItemsOfflineAttachments
import proton.android.pass.data.api.usecases.attachments.SetVaultOfflineAttachments
import proton.android.pass.domain.ShareId
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.AllowCellularDownloadPreference
import proton.android.pass.preferences.DownloadSharedItemsAttachmentsPreference
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import javax.inject.Inject

@HiltViewModel
class AttachmentConfigViewModel @Inject constructor(
    private val accountManager: AccountManager,
    observeVaults: ObserveVaults,
    private val setVaultOfflineAttachments: SetVaultOfflineAttachments,
    private val setSharedItemsOfflineAttachments: SetSharedItemsOfflineAttachments,
    private val preferencesRepository: UserPreferencesRepository,
    private val downloadScheduler: AttachmentDownloadScheduler,
    private val getVaultUsage: GetVaultUsage
) : ViewModel() {

    private val eventState =
        MutableStateFlow<AttachmentConfigEvent>(AttachmentConfigEvent.None)

    private val allVaultsEnabledState = MutableStateFlow(true)
    private val vaultOverrides = MutableStateFlow<Map<ShareId, Boolean>>(emptyMap())
    private val sharedItemsEnabledState = MutableStateFlow(true)
    private val cellularEnabledState = MutableStateFlow(false)
    private val vaultSizesState = MutableStateFlow<Map<ShareId, Long>>(emptyMap())
    private val fetchedShareIds = mutableSetOf<ShareId>()
    private val vaultsFlow = observeVaults(includeHidden = false)

    init {
        viewModelScope.launch {
            vaultsFlow
                .map { list -> list.map { it.shareId } }
                .distinctUntilChanged()
                .onEach { shareIds -> loadVaultSizes(shareIds) }
                .collect()
        }
    }

    internal val state: StateFlow<AttachmentConfigState> = combine(
        combine(
            vaultsFlow,
            allVaultsEnabledState,
            vaultOverrides,
            vaultSizesState
        ) { vaults, allVaults, overrides, sizes ->
            VaultsSnapshot(vaults, allVaults, overrides, sizes)
        },
        sharedItemsEnabledState,
        cellularEnabledState,
        eventState
    ) { snapshot, sharedItems, cellular, event ->
        val vaultItems = snapshot.vaults.map { vault ->
            VaultConfigItem(
                shareId = vault.shareId,
                name = vault.name,
                color = vault.color,
                icon = vault.icon,
                offlineEnabled = if (snapshot.allVaultsEnabled) {
                    true
                } else {
                    snapshot.overrides[vault.shareId] ?: false
                },
                totalSizeBytes = snapshot.sizes[vault.shareId]
            )
        }
        AttachmentConfigState(
            allVaultsEnabled = snapshot.allVaultsEnabled,
            vaults = vaultItems,
            sharedItemsEnabled = sharedItems,
            allowCellular = cellular,
            event = event
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AttachmentConfigState.Initial
    )

    private suspend fun loadVaultSizes(shareIds: List<ShareId>) {
        val missing = shareIds.filter { shareId -> shareId !in fetchedShareIds }
        if (missing.isEmpty()) return
        val userId = accountManager.getPrimaryUserId().firstOrNull() ?: return
        fetchedShareIds.addAll(missing)
        coroutineScope {
            val results = missing.map { shareId ->
                async {
                    val size = safeRunCatching { getVaultUsage(userId, shareId) }
                        .onFailure { error ->
                            PassLogger.w(TAG, "Error fetching usage for vault ${shareId.id}")
                            PassLogger.w(TAG, error)
                        }
                        .getOrNull()
                    shareId to size
                }
            }.awaitAll()
            val successes = results.mapNotNull { (id, size) -> size?.let { id to it } }
            if (successes.isNotEmpty()) {
                vaultSizesState.update { current -> current + successes }
            }
        }
    }

    private data class VaultsSnapshot(
        val vaults: List<proton.android.pass.domain.Vault>,
        val allVaultsEnabled: Boolean,
        val overrides: Map<ShareId, Boolean>,
        val sizes: Map<ShareId, Long>
    )

    internal fun onAllVaultsToggled(enabled: Boolean) {
        allVaultsEnabledState.update { enabled }
        if (enabled) {
            sharedItemsEnabledState.update { true }
        } else {
            vaultOverrides.update { emptyMap() }
            sharedItemsEnabledState.update { false }
        }
    }

    internal fun onVaultToggled(shareId: ShareId, enabled: Boolean) {
        vaultOverrides.update { it + (shareId to enabled) }
    }

    internal fun onSharedItemsToggled(enabled: Boolean) {
        sharedItemsEnabledState.update { enabled }
    }

    internal fun onCellularToggled(enabled: Boolean) {
        cellularEnabledState.update { enabled }
    }

    internal fun onStartClick() {
        viewModelScope.launch {
            val currentState = state.value
            val userId = accountManager.getPrimaryUserId().firstOrNull()
            if (userId == null) {
                PassLogger.w(TAG, "Cannot start: userId not available")
                return@launch
            }

            currentState.vaults.forEach { vault ->
                safeRunCatching {
                    setVaultOfflineAttachments(
                        userId,
                        vault.shareId,
                        vault.offlineEnabled
                    )
                }.onFailure { error ->
                    PassLogger.w(TAG, "Error setting offline attachments for vault ${vault.shareId.id}")
                    PassLogger.w(TAG, error)
                }
            }

            preferencesRepository.setSharedItemsDownloadPref(
                userId,
                DownloadSharedItemsAttachmentsPreference.from(
                    currentState.sharedItemsEnabled
                )
            )

            safeRunCatching {
                setSharedItemsOfflineAttachments(
                    userId,
                    currentState.sharedItemsEnabled
                )
            }

            preferencesRepository.setAllowCellularDownloadPref(
                userId,
                AllowCellularDownloadPreference.from(
                    currentState.allowCellular
                )
            )

            safeRunCatching { downloadScheduler.scheduleAll(userId, force = true) }
                .onFailure { error ->
                    PassLogger.w(TAG, "Error scheduling downloads")
                    PassLogger.w(TAG, error)
                }

            eventState.update { AttachmentConfigEvent.StartDownload }
        }
    }

    internal fun onEventConsumed() {
        eventState.update { AttachmentConfigEvent.None }
    }

    private companion object {

        private const val TAG = "AttachmentConfigViewModel"
    }
}

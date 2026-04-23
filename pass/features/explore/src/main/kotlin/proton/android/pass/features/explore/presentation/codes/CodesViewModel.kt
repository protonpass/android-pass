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

package proton.android.pass.features.explore.presentation.codes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.data.api.usecases.LoginTotpEntry
import proton.android.pass.data.api.usecases.ObserveLoginTotpEntries
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.totp.api.TotpManager
import javax.inject.Inject

@HiltViewModel
class CodesViewModel @Inject constructor(
    observeLoginTotpEntries: ObserveLoginTotpEntries,
    private val totpManager: TotpManager,
    private val clipboardManager: ClipboardManager,
    private val snackbarDispatcher: SnackbarDispatcher
) : ViewModel() {

    private val searchQueryFlow = MutableStateFlow("")
    private val inSearchModeFlow = MutableStateFlow(false)

    @Suppress("OPT_IN_USAGE")
    val uiState: StateFlow<CodesUiState> = observeLoginTotpEntries()
        .flatMapLatest { entries ->
            val codesFlow = if (entries.isEmpty()) {
                flowOf(emptyList<TotpManager.TotpWrapper>())
            } else {
                combineCodeFlows(entries)
            }
            combine(codesFlow, searchQueryFlow, inSearchModeFlow) { wrappers, query, inSearch ->
                val effectiveQuery = if (inSearch) query else ""
                val rows = entries.toRows(wrappers).filter { it.matches(effectiveQuery) }
                val showPerRowProgress = wrappers.any {
                    it.totalSeconds != CodesUiState.DEFAULT_TOTAL_SECONDS
                }
                CodesUiState(
                    isLoading = false,
                    rows = rows.toImmutableList(),
                    totalSeconds = wrappers.firstOrNull()?.totalSeconds
                        ?: CodesUiState.DEFAULT_TOTAL_SECONDS,
                    remainingSeconds = wrappers.firstOrNull()?.remainingSeconds
                        ?: CodesUiState.DEFAULT_TOTAL_SECONDS,
                    showPerRowProgress = showPerRowProgress,
                    searchQuery = effectiveQuery,
                    inSearchMode = inSearch
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS),
            initialValue = CodesUiState.Initial
        )

    fun onEvent(event: CodesUiEvent) {
        when (event) {
            CodesUiEvent.EnterSearch -> {
                searchQueryFlow.value = ""
                inSearchModeFlow.value = true
            }
            CodesUiEvent.StopSearch -> {
                searchQueryFlow.value = ""
                inSearchModeFlow.value = false
            }
            is CodesUiEvent.SearchQueryChange -> {
                searchQueryFlow.value = event.query
            }
            is CodesUiEvent.CodeClicked -> {
                clipboardManager.copyToClipboard(text = event.code, isSecure = false)
                viewModelScope.launch {
                    snackbarDispatcher(CodesSnackbarMessage.CodeCopied)
                }
            }
        }
    }

    private fun CodeRow.matches(query: String): Boolean {
        if (query.isEmpty()) return true
        return title.contains(query, ignoreCase = true) ||
            subtitle.contains(query, ignoreCase = true)
    }

    private fun combineCodeFlows(entries: List<LoginTotpEntry>): Flow<List<TotpManager.TotpWrapper>> =
        combine(entries.map { totpManager.observeCode(it.totpUri) }) { it.toList() }

    private fun List<LoginTotpEntry>.toRows(wrappers: List<TotpManager.TotpWrapper>): List<CodeRow> =
        mapIndexed { index, entry ->
            val wrapper = wrappers.getOrNull(index)
            CodeRow(
                rowId = "${entry.shareId.id}:${entry.itemId.id}:${entry.source}:$index",
                shareId = entry.shareId,
                itemId = entry.itemId,
                title = entry.itemTitle,
                subtitle = entry.subtitle,
                websites = entry.websites,
                packageName = entry.packageName,
                code = wrapper?.code.orEmpty(),
                totalSeconds = wrapper?.totalSeconds ?: CodesUiState.DEFAULT_TOTAL_SECONDS,
                remainingSeconds = wrapper?.remainingSeconds ?: CodesUiState.DEFAULT_TOTAL_SECONDS
            )
        }

    companion object {
        private const val STATE_TIMEOUT_MS = 5_000L
    }
}

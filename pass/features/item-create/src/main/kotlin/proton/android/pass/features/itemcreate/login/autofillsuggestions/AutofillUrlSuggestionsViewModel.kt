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

package proton.android.pass.features.itemcreate.login.autofillsuggestions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import proton.android.pass.data.api.url.HostInfo
import proton.android.pass.data.api.url.HostParser
import proton.android.pass.domain.AutofillUrlMode
import proton.android.pass.navigation.api.NavParamEncoder
import javax.inject.Inject

data class AutofillUrlSuggestionsUiState(
    val selectedMode: AutofillUrlMode,
    val previewEntries: List<Pair<String, Boolean>>
)

@HiltViewModel
class AutofillUrlSuggestionsViewModel @Inject constructor(
    hostParser: HostParser,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val urlIndex: Int = savedStateHandle.get<Int>(ARG_AUTOFILL_URL_INDEX) ?: 0

    val url: String = run {
        val encoded = savedStateHandle.get<String>(ARG_AUTOFILL_ENCODED_URL) ?: ""
        NavParamEncoder.decode(encoded).trim()
    }

    val initialMode: AutofillUrlMode = run {
        val modeStr = savedStateHandle.get<String>(ARG_AUTOFILL_CURRENT_MODE)
            ?: AutofillUrlMode.Default.name
        runCatching { AutofillUrlMode.valueOf(modeStr) }.getOrDefault(AutofillUrlMode.Default)
    }

    private val parsedHost: HostInfo.Host? =
        hostParser.parse(url).getOrNull() as? HostInfo.Host

    private val selectedMode = MutableStateFlow(initialMode)

    val state: StateFlow<AutofillUrlSuggestionsUiState> = selectedMode
        .map { mode ->
            AutofillUrlSuggestionsUiState(
                selectedMode = mode,
                previewEntries = buildPreviewEntries(mode)
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = AutofillUrlSuggestionsUiState(
                selectedMode = initialMode,
                previewEntries = buildPreviewEntries(initialMode)
            )
        )

    fun onModeSelected(mode: AutofillUrlMode) {
        selectedMode.value = mode
    }

    private fun buildPreviewEntries(mode: AutofillUrlMode): List<Pair<String, Boolean>> {
        val info = parsedHost ?: return emptyList()

        val domain = info.domain
        val tld = info.tld.value() ?: ""
        val subdomain = info.subdomain.value()
        val protocol = info.protocol.ifBlank { "https" }
        val rootDomain = if (tld.isNotEmpty()) "$domain.$tld" else domain
        val host = if (subdomain != null) "$subdomain.$rootDomain" else rootDomain
        val altTld = if (tld == "com") "net" else "com"

        return when (mode) {
            AutofillUrlMode.Default -> buildList {
                add("$protocol://$rootDomain" to true)
                if (subdomain != null) add("$protocol://$host" to true)
                val exampleSub = if (subdomain == "subdomain") "other.$rootDomain"
                else "subdomain.$rootDomain"
                add("$protocol://$exampleSub" to true)
                add("$protocol://$domain.$altTld" to false)
            }

            AutofillUrlMode.Exact -> buildList {
                add("$protocol://$host" to true)
                if (subdomain != null) {
                    add("$protocol://$rootDomain" to false)
                    val otherSub = if (subdomain == "login") "account" else "login"
                    add("$protocol://$otherSub.$rootDomain" to false)
                } else {
                    add("$protocol://sub.$host" to false)
                    add("$protocol://$domain.$altTld" to false)
                }
            }

            AutofillUrlMode.Never -> emptyList()

            else -> emptyList()
        }
    }
}

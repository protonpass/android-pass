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

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.domain.AutofillUrlMode

data class AutofillUrlSuggestionsPreviewInput(
    val url: String,
    val selectedMode: AutofillUrlMode,
    val previewEntries: List<Pair<String, Boolean>>,
    val hasAdvancedMode: Boolean = false
)

class AutofillUrlSuggestionsPreviewProvider :
    PreviewParameterProvider<AutofillUrlSuggestionsPreviewInput> {

    override val values: Sequence<AutofillUrlSuggestionsPreviewInput>
        get() = sequenceOf(
            AutofillUrlSuggestionsPreviewInput(
                url = "https://account.proton.me",
                selectedMode = AutofillUrlMode.Default,
                previewEntries = listOf(
                    "account.proton.me" to true,
                    "mail.proton.me" to true,
                    "proton.me" to true,
                    "other.example.com" to false
                )
            ),
            AutofillUrlSuggestionsPreviewInput(
                url = "https://account.proton.me",
                selectedMode = AutofillUrlMode.Exact,
                previewEntries = listOf(
                    "account.proton.me" to true,
                    "mail.proton.me" to false,
                    "proton.me" to false
                )
            ),
            AutofillUrlSuggestionsPreviewInput(
                url = "https://account.proton.me",
                selectedMode = AutofillUrlMode.Never,
                previewEntries = emptyList()
            ),
            AutofillUrlSuggestionsPreviewInput(
                url = "https://account.proton.me",
                selectedMode = AutofillUrlMode.RegularExpression,
                previewEntries = emptyList(),
                hasAdvancedMode = true
            )
        )
}

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

package proton.android.pass.features.security.center.darkweb.ui.summary

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.collections.immutable.persistentListOf
import proton.android.pass.domain.breach.BreachEmailId
import proton.android.pass.domain.breach.BreachId
import proton.android.pass.domain.breach.CustomEmailId
import proton.android.pass.features.security.center.darkweb.presentation.DarkWebEmailBreachState
import proton.android.pass.features.security.center.darkweb.presentation.DarkWebEmailsError
import proton.android.pass.features.security.center.shared.presentation.EmailBreachUiState

internal class DarkWebEmailBreachStateProvider :
    PreviewParameterProvider<DarkWebEmailBreachStatePreview> {
    override val values: Sequence<DarkWebEmailBreachStatePreview>
        get() = sequenceOf(
            DarkWebEmailBreachStatePreview(
                state = DarkWebEmailBreachState.Success(
                    emails = persistentListOf(
                        EmailBreachUiState(
                            id = BreachEmailId.Custom(
                                id = BreachId("1"),
                                customEmailId = CustomEmailId("1")
                            ),
                            email = "mail@proton.me",
                            count = 2,
                            breachDate = "2024-04-16T15:30:00Z",
                            isMonitored = true
                        ),
                        EmailBreachUiState(
                            id = BreachEmailId.Custom(
                                id = BreachId("2"),
                                customEmailId = CustomEmailId("2")
                            ),
                            email = "mail2@proton.me",
                            count = 2,
                            breachDate = "2024-04-16T15:30:00Z",
                            isMonitored = true
                        )
                    ),
                    enabledMonitoring = true
                ),
                summaryType = DarkWebSummaryType.Proton
            ),
            DarkWebEmailBreachStatePreview(
                state = DarkWebEmailBreachState.Success(
                    emails = persistentListOf(
                        EmailBreachUiState(
                            id = BreachEmailId.Custom(
                                id = BreachId("1"),
                                customEmailId = CustomEmailId("1")
                            ),
                            email = "mail@proton.me",
                            count = 2,
                            breachDate = "2024-04-16T15:30:00Z",
                            isMonitored = true
                        ),
                        EmailBreachUiState(
                            id = BreachEmailId.Custom(
                                id = BreachId("2"),
                                customEmailId = CustomEmailId("2")
                            ),
                            email = "mail2@proton.me",
                            count = 2,
                            breachDate = "2024-04-16T15:30:00Z",
                            isMonitored = false
                        )
                    ),
                    enabledMonitoring = true
                ),
                summaryType = DarkWebSummaryType.Proton
            ),
            DarkWebEmailBreachStatePreview(
                state = DarkWebEmailBreachState.Success(
                    emails = persistentListOf(),
                    enabledMonitoring = true
                ),
                summaryType = DarkWebSummaryType.Proton
            ),
            DarkWebEmailBreachStatePreview(
                state = DarkWebEmailBreachState.Success(
                    emails = persistentListOf(),
                    enabledMonitoring = false
                ),
                summaryType = DarkWebSummaryType.Proton
            ),
            DarkWebEmailBreachStatePreview(
                state = DarkWebEmailBreachState.Loading,
                summaryType = DarkWebSummaryType.Proton
            ),
            DarkWebEmailBreachStatePreview(
                state = DarkWebEmailBreachState.Error(DarkWebEmailsError.CannotLoad),
                summaryType = DarkWebSummaryType.Proton
            ),
            DarkWebEmailBreachStatePreview(
                state = DarkWebEmailBreachState.Success(
                    emails = persistentListOf(),
                    enabledMonitoring = false
                ),
                summaryType = DarkWebSummaryType.Alias
            ),
            DarkWebEmailBreachStatePreview(
                state = DarkWebEmailBreachState.Success(
                    emails = persistentListOf(),
                    enabledMonitoring = true
                ),
                summaryType = DarkWebSummaryType.Alias
            ),
            DarkWebEmailBreachStatePreview(
                state = DarkWebEmailBreachState.Error(DarkWebEmailsError.CannotLoad),
                summaryType = DarkWebSummaryType.Alias
            )
        )
}


internal data class DarkWebEmailBreachStatePreview(
    val state: DarkWebEmailBreachState,
    val summaryType: DarkWebSummaryType
)

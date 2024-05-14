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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.collections.immutable.persistentListOf
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.domain.breach.BreachEmailId
import proton.android.pass.domain.breach.BreachId
import proton.android.pass.domain.breach.CustomEmailId
import proton.android.pass.features.security.center.darkweb.presentation.DarkWebEmailBreachState
import proton.android.pass.features.security.center.darkweb.presentation.DarkWebEmailsError
import proton.android.pass.features.security.center.darkweb.ui.DarkWebUiEvent
import proton.android.pass.features.security.center.shared.presentation.EmailBreachUiState
import proton.android.pass.features.security.center.shared.ui.rows.EmailBreachRow

@Composable
internal fun EmailBreachSection(
    modifier: Modifier = Modifier,
    state: DarkWebEmailBreachState,
    summaryType: DarkWebSummaryType,
    isClickable: Boolean,
    onEvent: (DarkWebUiEvent) -> Unit
) {
    Column(
        modifier = modifier
    ) {
        EmailBreachHeader(
            summaryType = summaryType,
            isClickable = isClickable,
            onEvent = onEvent,
            state = state
        )

        val listSize = state.list().take(10).size
        Column(
            modifier = Modifier
                .padding(horizontal = Spacing.medium)
                .roundedContainerNorm()
                .padding(vertical = Spacing.small)
        ) {
            repeat(listSize) { index ->
                EmailBreachRow(
                    emailBreachUiState = state.list()[index],
                    onClick = {
                        when (it.id) {
                            is BreachEmailId.Alias -> onEvent(
                                DarkWebUiEvent.OnShowAliasEmailReportClick(
                                    id = it.id,
                                    email = it.email,
                                    breachCount = it.count
                                )
                            )

                            is BreachEmailId.Custom -> {
                                // It won't reach this point
                            }

                            is BreachEmailId.Proton -> onEvent(
                                DarkWebUiEvent.OnShowProtonEmailReportClick(
                                    id = it.id,
                                    email = it.email,
                                    breachCount = it.count
                                )
                            )
                        }
                    },
                    globalMonitorEnabled = state.enabledMonitoring()
                )
                if (index < listSize - 1) {
                    PassDivider(modifier = Modifier.padding(horizontal = Spacing.medium))
                }
            }
        }

        if (listSize > 0) {
            Spacer(modifier = Modifier.height(Spacing.small))
        }
    }
}

internal class DarkWebEmailBreachStatePreviewProvider :
    PreviewParameterProvider<DarkWebEmailBreachState> {
    override val values: Sequence<DarkWebEmailBreachState>
        get() = sequenceOf(
            DarkWebEmailBreachState.Success(
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
            DarkWebEmailBreachState.Success(
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
            DarkWebEmailBreachState.Success(
                emails = persistentListOf(),
                enabledMonitoring = true
            ),
            DarkWebEmailBreachState.Success(
                emails = persistentListOf(),
                enabledMonitoring = false
            ),
            DarkWebEmailBreachState.Loading,
            DarkWebEmailBreachState.Error(DarkWebEmailsError.CannotLoad)
        )
}

internal class ThemedDarkWebEmailBreachStatePreviewProvider :
    ThemePairPreviewProvider<DarkWebEmailBreachState>(DarkWebEmailBreachStatePreviewProvider())

@Preview
@Composable
internal fun EmailBreachSectionPreview(
    @PreviewParameter(ThemedDarkWebEmailBreachStatePreviewProvider::class) input: Pair<Boolean, DarkWebEmailBreachState>
) {
    PassTheme(isDark = input.first) {
        Surface {
            EmailBreachSection(
                state = input.second,
                summaryType = DarkWebSummaryType.Proton,
                onEvent = {},
                isClickable = true
            )
        }
    }
}

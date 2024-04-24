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

package proton.android.pass.features.security.center.darkweb.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultWeak
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.features.security.center.R
import proton.android.pass.features.security.center.darkweb.presentation.CustomEmailUiStatus
import proton.android.pass.features.security.center.darkweb.presentation.DarkWebCustomEmailsState
import proton.android.pass.features.security.center.darkweb.presentation.DarkWebEmailsError
import proton.android.pass.features.security.center.darkweb.presentation.DarkWebUiState
import proton.android.pass.features.security.center.darkweb.ui.customemails.list.CustomEmailItem
import proton.android.pass.features.security.center.darkweb.ui.customemails.list.CustomEmailsHeader
import proton.android.pass.features.security.center.darkweb.ui.summary.DarkWebSummary
import proton.android.pass.features.security.center.shared.ui.bars.SecurityCenterTopBar

@Composable
internal fun DarkWebContent(
    modifier: Modifier = Modifier,
    state: DarkWebUiState,
    onEvent: (DarkWebUiEvent) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            val subtitle = state.lastCheckTime.value()?.let { lastCheckTime ->
                stringResource(
                    id = R.string.security_center_dark_web_monitor_top_bar_subtitle,
                    lastCheckTime
                )
            }
            SecurityCenterTopBar(
                title = stringResource(R.string.security_center_dark_web_monitor_top_bar_title),
                subtitle = subtitle,
                onUpClick = { onEvent(DarkWebUiEvent.OnUpClick) },
                actions = { DarkWebStatusIndicator(status = state.darkWebStatus) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(vertical = Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                DarkWebSummary(
                    modifier = Modifier.padding(horizontal = Spacing.medium),
                    protonEmailsState = state.protonEmailState,
                    aliasEmailsState = state.aliasEmailState,
                    onEvent = onEvent
                )
            }
            item {
                CustomEmailsHeader(
                    modifier = Modifier.padding(horizontal = Spacing.medium),
                    onAddClick = { onEvent(DarkWebUiEvent.OnNewCustomEmailClick) }
                )
            }

            when (state.customEmailState) {
                is DarkWebCustomEmailsState.Error -> {
                    item {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.medium),
                            text = when (state.customEmailState.reason) {
                                DarkWebEmailsError.CannotLoad -> stringResource(
                                    R.string.security_center_dark_web_monitor_custom_emails_loading_error
                                )

                                DarkWebEmailsError.Unknown -> stringResource(
                                    R.string.security_center_dark_web_monitor_custom_emails_unknown_error
                                )
                            }
                        )
                    }

                }

                DarkWebCustomEmailsState.Loading -> {
                    item { CircularProgressIndicator(Modifier.size(48.dp)) }
                }

                is DarkWebCustomEmailsState.Success -> {
                    items(items = state.customEmailState.emails, key = { it.email }) { itemEmail ->
                        CustomEmailItem(
                            email = itemEmail,
                            onAddClick = {},
                            onDetailClick = { id ->
                                onEvent(
                                    DarkWebUiEvent.OnCustomEmailReportClick(
                                        id = id,
                                        email = itemEmail.email,
                                        breachCount = (itemEmail.status as? CustomEmailUiStatus.Verified)
                                            ?.breachesDetected
                                            ?: 0
                                    )
                                )
                            },
                            onOptionsClick = {
                                val event = DarkWebUiEvent.OnUnverifiedEmailOptionsClick(
                                    id = it,
                                    email = itemEmail.email
                                )
                                onEvent(event)
                            }
                        )
                    }

                    if (state.customEmailState.emails.isNotEmpty() && state.customEmailState.suggestions.isNotEmpty()) {
                        item {
                            Text(
                                modifier = Modifier.padding(Spacing.medium),
                                text = stringResource(
                                    id = R.string.security_center_dark_web_monitor_custom_emails_suggestions
                                ),
                                style = ProtonTheme.typography.defaultWeak
                            )
                        }
                    }

                    items(
                        items = state.customEmailState.suggestions,
                        key = { it.email }
                    ) { itemEmail ->
                        CustomEmailItem(
                            email = itemEmail,
                            onAddClick = {
                                onEvent(DarkWebUiEvent.OnAddCustomEmailClick(itemEmail.email))
                            },
                            onDetailClick = {},
                            onOptionsClick = {}
                        )
                    }
                }
            }
        }
    }
}

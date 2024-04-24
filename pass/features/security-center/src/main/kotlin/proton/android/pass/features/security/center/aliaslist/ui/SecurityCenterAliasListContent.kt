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

package proton.android.pass.features.security.center.aliaslist.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultWeak
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.buttons.CircleIconButton
import proton.android.pass.composecomponents.impl.loading.Loading
import proton.android.pass.domain.breach.BreachEmailId
import proton.android.pass.features.security.center.R
import proton.android.pass.features.security.center.aliaslist.presentation.AliasListState
import proton.android.pass.features.security.center.aliaslist.presentation.SecurityCenterAliasListState
import proton.android.pass.features.security.center.aliaslist.ui.SecurityCenterAliasListUiEvent.EmailBreachClick
import proton.android.pass.features.security.center.shared.ui.bars.SecurityCenterTopBar
import proton.android.pass.features.security.center.shared.ui.rows.EmailBreachRow
import me.proton.core.presentation.R as CoreR

@Composable
internal fun SecurityCenterAliasListContent(
    modifier: Modifier = Modifier,
    state: SecurityCenterAliasListState,
    onUiEvent: (SecurityCenterAliasListUiEvent) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            SecurityCenterTopBar(
                title = stringResource(R.string.security_center_alias_list_top_bar_title),
                onUpClick = { onUiEvent(SecurityCenterAliasListUiEvent.Back) },
                actions = {
                    CircleIconButton(
                        iconPainter = painterResource(CoreR.drawable.ic_proton_three_dots_vertical),
                        size = 40,
                        backgroundColor = PassTheme.colors.interactionNormMinor1,
                        tintColor = PassTheme.colors.interactionNormMajor2,
                        iconContentDescription = "",
                        onClick = { onUiEvent(SecurityCenterAliasListUiEvent.OptionsClick) }
                    )
                }
            )
        }
    ) { innerPaddingValues ->
        Column(
            modifier = Modifier
                .background(PassTheme.colors.backgroundNorm)
                .padding(paddingValues = innerPaddingValues)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val list = state.listState) {
                is AliasListState.Loading -> Loading(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.medium)
                )

                is AliasListState.Error -> Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.medium),
                    text = stringResource(R.string.security_center_alias_list_error),
                    style = ProtonTheme.typography.body2Regular,
                    color = ProtonTheme.colors.notificationError
                )

                is AliasListState.Success -> LazyColumn(
                    modifier = Modifier.padding(vertical = Spacing.small)
                ) {
                    if (!state.isGlobalMonitorEnabled) {
                        item {
                            Text(
                                modifier = Modifier.padding(Spacing.medium),
                                text = stringResource(R.string.security_center_alias_list_included_in_monitoring),
                                style = ProtonTheme.typography.defaultWeak
                            )
                        }
                    }
                    items(list.includedBreachedEmails, key = { it.email }) { itemState ->
                        EmailBreachRow(
                            emailBreachUiState = itemState,
                            globalMonitorEnabled = state.isGlobalMonitorEnabled,
                            onClick = {
                                onUiEvent(
                                    EmailBreachClick(
                                        id = itemState.id as BreachEmailId.Alias,
                                        email = itemState.email,
                                        breachCount = itemState.count
                                    )
                                )
                            }
                        )
                    }
                    if (list.includedMonitoredEmails.isNotEmpty()) {
                        item {
                            Text(
                                modifier = Modifier.padding(horizontal = Spacing.medium),
                                text = stringResource(R.string.security_center_alias_list_monitored),
                                style = ProtonTheme.typography.body2Regular
                            )
                        }
                    }
                    items(list.includedMonitoredEmails, key = { it.email }) { itemState ->
                        EmailBreachRow(
                            emailBreachUiState = itemState,
                            globalMonitorEnabled = state.isGlobalMonitorEnabled,
                            onClick = {
                                onUiEvent(
                                    EmailBreachClick(
                                        id = itemState.id as BreachEmailId.Alias,
                                        email = itemState.email,
                                        breachCount = itemState.count
                                    )
                                )
                            }
                        )
                    }
                    if (list.excludedEmails.isNotEmpty()) {
                        item {
                            Text(
                                modifier = Modifier.padding(horizontal = Spacing.medium),
                                text = stringResource(R.string.security_center_alias_list_excluded_from_monitoring),
                                style = ProtonTheme.typography.body2Regular
                            )
                        }
                    }
                    items(list.excludedEmails, key = { it.email }) { itemState ->
                        EmailBreachRow(
                            emailBreachUiState = itemState,
                            globalMonitorEnabled = state.isGlobalMonitorEnabled,
                            onClick = {
                                onUiEvent(
                                    EmailBreachClick(
                                        id = itemState.id as BreachEmailId.Alias,
                                        email = itemState.email,
                                        breachCount = itemState.count
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

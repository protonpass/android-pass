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

package proton.android.pass.features.security.center.protonlist.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultWeak
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.buttons.CircleIconButton
import proton.android.pass.composecomponents.impl.loading.Loading
import proton.android.pass.composecomponents.impl.topbar.PassExtendedTopBar
import proton.android.pass.domain.breach.BreachEmailId
import proton.android.pass.features.security.center.R
import proton.android.pass.features.security.center.protonlist.presentation.ProtonListState
import proton.android.pass.features.security.center.protonlist.presentation.SecurityCenterProtonListState
import proton.android.pass.features.security.center.protonlist.ui.SecurityCenterProtonListUiEvent.EmailBreachClick
import proton.android.pass.features.security.center.shared.ui.rows.EmailBreachRow
import me.proton.core.presentation.R as CoreR

@Composable
internal fun SecurityCenterProtonListContent(
    modifier: Modifier = Modifier,
    state: SecurityCenterProtonListState,
    onUiEvent: (SecurityCenterProtonListUiEvent) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            PassExtendedTopBar(
                title = stringResource(R.string.security_center_proton_list_top_bar_title),
                onUpClick = { onUiEvent(SecurityCenterProtonListUiEvent.Back) },
                actions = {
                    CircleIconButton(
                        drawableRes = CoreR.drawable.ic_proton_three_dots_vertical,
                        size = 40,
                        backgroundColor = PassTheme.colors.interactionNormMinor1,
                        tintColor = PassTheme.colors.interactionNormMajor2,
                        iconContentDescription = stringResource(id = R.string.security_center_proton_list_options_menu),
                        onClick = { onUiEvent(SecurityCenterProtonListUiEvent.OptionsClick) }
                    )
                }
            )
        }
    ) { innerPaddingValues ->
        Column(
            modifier = Modifier
                .background(PassTheme.colors.backgroundNorm)
                .padding(paddingValues = innerPaddingValues)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val list = state.listState) {
                is ProtonListState.Loading -> Loading(Modifier.weight(1f))

                is ProtonListState.Error -> Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.medium),
                    text = stringResource(R.string.security_center_proton_list_error),
                    style = ProtonTheme.typography.body2Regular,
                    color = ProtonTheme.colors.notificationError
                )

                is ProtonListState.Success -> LazyColumn(
                    modifier = Modifier.padding(vertical = Spacing.small)
                ) {
                    if (!state.isGlobalMonitorEnabled) {
                        item {
                            Text(
                                modifier = Modifier.padding(Spacing.medium),
                                text = stringResource(R.string.security_center_proton_list_included_in_monitoring),
                                style = ProtonTheme.typography.defaultWeak
                            )
                        }
                    }
                    items(list.includedEmails, key = { it.email }) { itemState ->
                        EmailBreachRow(
                            emailBreachUiState = itemState,
                            globalMonitorEnabled = state.isGlobalMonitorEnabled,
                            onClick = {
                                onUiEvent(
                                    EmailBreachClick(
                                        id = itemState.id as BreachEmailId.Proton,
                                        email = itemState.email
                                    )
                                )
                            }
                        )
                    }
                    if (list.excludedEmails.isNotEmpty()) {
                        item {
                            Text(
                                modifier = Modifier.padding(horizontal = Spacing.medium),
                                text = stringResource(R.string.security_center_proton_list_excluded_from_monitoring),
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
                                        id = itemState.id as BreachEmailId.Proton,
                                        email = itemState.email
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

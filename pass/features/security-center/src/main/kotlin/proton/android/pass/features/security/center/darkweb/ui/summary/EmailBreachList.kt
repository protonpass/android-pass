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
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.domain.breach.BreachEmailId
import proton.android.pass.features.security.center.darkweb.presentation.DarkWebEmailBreachState
import proton.android.pass.features.security.center.darkweb.ui.DarkWebUiEvent
import proton.android.pass.features.security.center.shared.ui.rows.EmailBreachRow

@Composable
internal fun EmailBreachEmptyList(
    modifier: Modifier = Modifier,
    listSize: Int,
    state: DarkWebEmailBreachState,
    onEvent: (DarkWebUiEvent) -> Unit
) {
    Column(
        modifier = modifier
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
                                email = it.email
                            )
                        )

                        is BreachEmailId.Custom -> {
                            // It won't reach this point
                        }

                        is BreachEmailId.Proton -> onEvent(
                            DarkWebUiEvent.OnShowProtonEmailReportClick(
                                id = it.id,
                                email = it.email
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
}

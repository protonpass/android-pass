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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.body3Weak
import proton.android.pass.composecomponents.impl.container.Circle
import proton.android.pass.domain.breach.BreachEmailId
import proton.android.pass.features.security.center.R
import proton.android.pass.features.security.center.darkweb.presentation.EmailBreachUiState
import proton.android.pass.features.security.center.darkweb.ui.DarkWebUiEvent

@Composable
internal fun EmailBreachRow(
    modifier: Modifier = Modifier,
    emailBreachUiState: EmailBreachUiState,
    onEvent: (DarkWebUiEvent) -> Unit
) {
    Column(
        modifier = modifier
            .clickable(
                onClick = {
                    when (emailBreachUiState.id) {
                        is BreachEmailId.Alias -> onEvent(
                            DarkWebUiEvent.OnShowAliasEmailReportClick(
                                id = emailBreachUiState.id,
                                email = emailBreachUiState.email,
                                breachCount = emailBreachUiState.count
                            )
                        )

                        is BreachEmailId.Custom -> {
                            // It won't reach this point
                        }
                        is BreachEmailId.Proton -> onEvent(
                            DarkWebUiEvent.OnShowProtonEmailReportClick(
                                id = emailBreachUiState.id,
                                email = emailBreachUiState.email,
                                breachCount = emailBreachUiState.count
                            )
                        )
                    }
                }
            )
            .padding(horizontal = Spacing.medium, vertical = Spacing.small)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            Column(
                modifier = Modifier.weight(weight = 1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
            ) {
                Text(
                    text = emailBreachUiState.email,
                    style = ProtonTheme.typography.defaultNorm.copy(
                        color = PassTheme.colors.passwordInteractionNormMajor1
                    )
                )
                emailBreachUiState.breachDate?.let { breachDate ->
                    Text(
                        text = stringResource(
                            R.string.security_center_dark_web_monitor_latest_breach_on,
                            breachDate
                        ),
                        style = PassTheme.typography.body3Weak()
                    )
                }
            }
            if (emailBreachUiState.count > 0) {
                Circle(
                    backgroundColor = PassTheme.colors.passwordInteractionNormMinor1
                ) {
                    Text(
                        text = emailBreachUiState.count.toString(),
                        color = PassTheme.colors.passwordInteractionNormMajor1,
                        style = ProtonTheme.typography.body1Regular
                    )
                }
            }
            Icon(
                painter = painterResource(proton.android.pass.composecomponents.impl.R.drawable.ic_chevron_tiny_right),
                contentDescription = null,
                tint = PassTheme.colors.passwordInteractionNormMajor1
            )
        }
    }
}

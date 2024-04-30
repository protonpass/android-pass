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

package proton.android.pass.features.security.center.shared.ui.rows

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
import androidx.compose.ui.text.TextStyle
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.commonui.api.body3Norm
import proton.android.pass.commonui.api.body3Weak
import proton.android.pass.commonui.api.defaultTint
import proton.android.pass.composecomponents.impl.container.Circle
import proton.android.pass.features.security.center.R
import proton.android.pass.features.security.center.shared.presentation.EmailBreachUiState
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun EmailBreachRow(
    modifier: Modifier = Modifier,
    emailBreachUiState: EmailBreachUiState,
    globalMonitorEnabled: Boolean,
    onClick: (EmailBreachUiState) -> Unit
) {
    Column(
        modifier = modifier
            .applyIf(
                condition = globalMonitorEnabled,
                ifTrue = { clickable(onClick = { onClick(emailBreachUiState) }) }
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

                when {
                    emailBreachUiState.hasBreaches && emailBreachUiState.isMonitored -> EmailAndBreachDate(
                        email = emailBreachUiState.email,
                        breachDate = emailBreachUiState.breachDate,
                        textStyle = ProtonTheme.typography.body1Regular
                            .copy(color = PassTheme.colors.passwordInteractionNormMajor1),
                        dateStyle = PassTheme.typography.body3Weak(),
                        hasBreaches = true
                    )

                    !emailBreachUiState.hasBreaches && emailBreachUiState.isMonitored -> EmailAndBreachDate(
                        email = emailBreachUiState.email,
                        breachDate = emailBreachUiState.breachDate,
                        textStyle = ProtonTheme.typography.body1Regular,
                        dateStyle = PassTheme.typography.body3Norm()
                            .copy(color = PassTheme.colors.cardInteractionNormMajor2),
                        hasBreaches = false
                    )

                    else -> EmailAndBreachDate(
                        email = emailBreachUiState.email,
                        breachDate = emailBreachUiState.breachDate,
                        textStyle = ProtonTheme.typography.body1Regular,
                        dateStyle = PassTheme.typography.body3Weak(),
                        hasBreaches = false
                    )
                }
            }
            if (emailBreachUiState.hasBreaches && emailBreachUiState.isMonitored) {
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

            if (globalMonitorEnabled) {
                Icon(
                    painter = painterResource(CompR.drawable.ic_chevron_tiny_right),
                    contentDescription = null,
                    tint = PassTheme.colors.passwordInteractionNormMajor1
                        .takeIf { emailBreachUiState.hasBreaches }
                        ?: defaultTint()
                )
            }
        }
    }
}

@Composable
fun EmailAndBreachDate(
    email: String,
    breachDate: String?,
    textStyle: TextStyle,
    dateStyle: TextStyle,
    hasBreaches: Boolean
) {
    Text(
        text = email,
        style = textStyle
    )
    if (hasBreaches) {
        breachDate?.let {
            Text(
                text = stringResource(
                    R.string.security_center_dark_web_monitor_latest_breach_on,
                    it
                ),
                style = dateStyle
            )
        }
    } else {
        Text(
            text = stringResource(R.string.security_center_proton_list_no_breaches_detected),
            style = PassTheme.typography.body3Norm()
                .copy(color = PassTheme.colors.cardInteractionNormMajor2)
        )
    }
}

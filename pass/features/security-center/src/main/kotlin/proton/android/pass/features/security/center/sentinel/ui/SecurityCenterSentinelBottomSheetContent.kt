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

package proton.android.pass.features.security.center.sentinel.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.heroNorm
import proton.android.pass.composecomponents.impl.buttons.PassCircleButton
import proton.android.pass.domain.features.PaidFeature
import proton.android.pass.features.security.center.R
import proton.android.pass.features.security.center.sentinel.presentation.SecurityCenterSentinelState
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun SecurityCenterSentinelBottomSheetContent(
    modifier: Modifier = Modifier,
    onUiEvent: (SecurityCenterSentinelUiEvent) -> Unit,
    state: SecurityCenterSentinelState
) = with(state) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = PassTheme.colors.backgroundNorm)
            .padding(all = Spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_proton_sentinel),
            contentDescription = null
        )

        Text(
            text = stringResource(id = R.string.security_center_sentinel_title),
            textAlign = TextAlign.Center,
            style = PassTheme.typography.heroNorm()
        )

        Text(
            text = stringResource(id = R.string.security_center_sentinel_subtitle),
            textAlign = TextAlign.Center,
            style = ProtonTheme.typography.body1Regular,
            color = ProtonTheme.colors.textWeak
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
        ) {
            PassCircleButton(
                text = if (isSentinelEnabled) {
                    R.string.security_center_sentinel_button_disable
                } else {
                    R.string.security_center_sentinel_button_enable
                }.let { textResId -> stringResource(id = textResId) },
                onClick = {
                    when {
                        isFreeUser -> onUiEvent(
                            SecurityCenterSentinelUiEvent.OnUpsell(PaidFeature.SentinelFree)
                        )

                        canEnableSentinel == true -> if (isSentinelEnabled) {
                            onUiEvent(SecurityCenterSentinelUiEvent.OnDisableSentinel)
                        } else {
                            onUiEvent(SecurityCenterSentinelUiEvent.OnEnableSentinel)
                        }

                        canEnableSentinel == false -> onUiEvent(
                            SecurityCenterSentinelUiEvent.OnUpsell(PaidFeature.SentinelEssential)
                        )

                        else -> {}
                    }
                },
                isLoading = isLoadingState.value()
            )

            PassCircleButton(
                backgroundColor = PassTheme.colors.interactionNormMinor1,
                text = stringResource(id = CompR.string.action_learn_more),
                textColor = PassTheme.colors.interactionNormMajor2,
                onClick = { onUiEvent(SecurityCenterSentinelUiEvent.OnLearnMore) }
            )
        }
    }
}

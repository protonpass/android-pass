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

package proton.android.pass.features.upsell.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.features.upsell.navigation.UpsellNavDestination
import proton.android.pass.features.upsell.presentation.UpsellState

@Composable
internal fun UpsellContent(
    modifier: Modifier = Modifier,
    onNavigated: (UpsellNavDestination) -> Unit,
    state: UpsellState
) = with(state) {
    Scaffold(
        modifier = modifier,
        bottomBar = {
            UpsellFooter(
                modifier = Modifier.padding(
                    horizontal = Spacing.large,
                    vertical = Spacing.medium
                ),
                submitText = stringResource(id = state.submitText),
                onUpgradeClick = {
                    if (state.canUpgrade) {
                        onNavigated(UpsellNavDestination.Upgrade)
                    } else {
                        onNavigated(UpsellNavDestination.Subscription)
                    }
                },
                onNotNowClick = { onNavigated(UpsellNavDestination.CloseScreen) }
            )
        }
    ) { innerPaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = PassTheme.colors.backgroundNorm)
                .padding(paddingValues = innerPaddingValues)
                .padding(
                    start = Spacing.medium,
                    top = Spacing.extraLarge,
                    end = Spacing.medium
                )
                .verticalScroll(state = rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(space = Spacing.medium)
        ) {
            UpsellHeader(
                modifier = Modifier.padding(horizontal = Spacing.medium),
                imageResId = logo,
                titleResId = title,
                subtitleResId = subtitle
            )

            UpsellFeatures(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.medium),
                features = features
            )
        }
    }
}

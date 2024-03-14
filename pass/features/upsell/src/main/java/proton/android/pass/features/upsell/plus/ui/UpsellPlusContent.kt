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

package proton.android.pass.features.upsell.plus.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.features.upsell.R
import proton.android.pass.features.upsell.shared.navigation.UpsellNavDestination
import proton.android.pass.features.upsell.shared.ui.UpsellButton
import proton.android.pass.features.upsell.shared.ui.UpsellFeatureModel
import proton.android.pass.features.upsell.shared.ui.UpsellFeatures
import proton.android.pass.features.upsell.shared.ui.UpsellHeader
import proton.android.pass.features.upsell.shared.ui.UpsellTopBar

@Composable
internal fun UpsellPlusContent(
    modifier: Modifier = Modifier,
    features: ImmutableList<UpsellFeatureModel>,
    onNavigate: (UpsellNavDestination) -> Unit
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(PassTheme.colors.backgroundStrong),
        topBar = {
            UpsellTopBar(
                onUpClick = { onNavigate(UpsellNavDestination.Close) }
            )
        }
    ) { innerPaddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues = innerPaddingValues)
                .padding(horizontal = Spacing.medium)
                .verticalScroll(state = rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.large)
        ) {
            UpsellHeader(
                imageResId = R.drawable.logo_upsell,
                titleResId = R.string.upsell_plus_title,
                subtitleResId = R.string.upsell_plus_subtitle
            )

            UpsellFeatures(features = features)

            UpsellButton(
                onClick = { onNavigate(UpsellNavDestination.Upgrade) }
            )
        }
    }
}

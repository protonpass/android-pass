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

package proton.android.pass.features.security.center.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import proton.android.pass.commonpresentation.api.bars.bottom.home.presentation.HomeBottomBarEvent
import proton.android.pass.commonpresentation.api.bars.bottom.home.presentation.HomeBottomBarSelection
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.heroNorm
import proton.android.pass.composecomponents.impl.bottombar.PassHomeBottomBar
import proton.android.pass.features.security.center.R
import proton.android.pass.features.security.center.home.navigation.SecurityCenterHomeNavDestination

@Composable
internal fun SecurityCenterHomeContent(
    modifier: Modifier = Modifier,
    onNavigated: (SecurityCenterHomeNavDestination) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            ProtonTopAppBar(
                backgroundColor = PassTheme.colors.backgroundStrong,
                title = {
                    Text(
                        text = stringResource(R.string.security_center_home_top_bar_title),
                        style = PassTheme.typography.heroNorm()
                    )
                }
            )
        },
        bottomBar = {
            PassHomeBottomBar(
                selection = HomeBottomBarSelection.SecurityCenter,
                onEvent = { homeBottomBarEvent ->
                    when (homeBottomBarEvent) {
                        HomeBottomBarEvent.OnHomeSelected -> SecurityCenterHomeNavDestination.Home
                        HomeBottomBarEvent.OnNewItemSelected -> SecurityCenterHomeNavDestination.NewItem
                        HomeBottomBarEvent.OnProfileSelected -> SecurityCenterHomeNavDestination.Profile
                        HomeBottomBarEvent.OnSecurityCenterSelected -> null
                    }.also { destination -> destination?.let(onNavigated) }
                }
            )
        }
    ) { innerPaddingValues ->
        Column(
            modifier = Modifier
                .background(PassTheme.colors.backgroundStrong)
                .padding(paddingValues = innerPaddingValues)
                .verticalScroll(rememberScrollState())
        ) {

        }
    }
}

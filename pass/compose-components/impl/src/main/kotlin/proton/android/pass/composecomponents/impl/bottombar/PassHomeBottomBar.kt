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

package proton.android.pass.composecomponents.impl.bottombar

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.core.presentation.R
import proton.android.pass.commonpresentation.api.bars.bottom.home.presentation.BottomBarSelection
import proton.android.pass.commonpresentation.api.bars.bottom.home.presentation.HomeBottomBarEvent
import proton.android.pass.commonpresentation.api.bars.bottom.home.presentation.HomeBottomBarState
import proton.android.pass.commonpresentation.api.bars.bottom.home.presentation.HomeBottomBarViewModel
import proton.android.pass.commonpresentation.impl.bars.bottom.home.presentation.HomeBottomBarViewModelImpl
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.domain.PlanType
import proton.android.pass.preferences.monitor.MonitorStatusPreference
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
fun PassHomeBottomBar(
    modifier: Modifier = Modifier,
    selection: BottomBarSelection,
    onEvent: (HomeBottomBarEvent) -> Unit,
    viewModel: HomeBottomBarViewModel = hiltViewModel<HomeBottomBarViewModelImpl>()
) = with(viewModel) {
    val state by state.collectAsStateWithLifecycle()

    HomeBottomBarContent(
        modifier = modifier,
        selection = selection,
        onEvent = onEvent,
        state = state
    )
}

@Composable
fun HomeBottomBarContent(
    modifier: Modifier = Modifier,
    selection: BottomBarSelection,
    onEvent: (HomeBottomBarEvent) -> Unit,
    state: HomeBottomBarState
) = with(state) {
    val bottomPadding = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()
    BottomNavigation(
        modifier = modifier,
        backgroundColor = PassTheme.colors.bottomBarBackground
    ) {
        BottomNavigationItem(
            modifier = Modifier.padding(bottom = bottomPadding),
            selected = selection == BottomBarSelection.Home,
            selectedContentColor = PassTheme.colors.interactionNormMajor2,
            unselectedContentColor = PassTheme.colors.textNorm,
            onClick = { onEvent(HomeBottomBarEvent.OnHomeSelected) },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_proton_list_bullets),
                    contentDescription = stringResource(CompR.string.bottom_bar_list_items_icon_content_description)
                )
            }
        )

        BottomNavigationItem(
            modifier = Modifier.padding(bottom = bottomPadding),
            selected = selection == BottomBarSelection.ItemCreate,
            selectedContentColor = PassTheme.colors.interactionNormMajor2,
            unselectedContentColor = PassTheme.colors.textNorm,
            onClick = { onEvent(HomeBottomBarEvent.OnNewItemSelected) },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_proton_plus),
                    contentDescription = stringResource(CompR.string.bottom_bar_add_item_icon_content_description)
                )
            }
        )

        BottomNavigationItem(
            modifier = Modifier.padding(bottom = bottomPadding),
            selected = selection == BottomBarSelection.SecurityCenter,
            selectedContentColor = PassTheme.colors.interactionNormMajor2,
            unselectedContentColor = PassTheme.colors.textNorm,
            onClick = { onEvent(HomeBottomBarEvent.OnSecurityCenterSelected) },
            icon = {
                PassHomeBottomBarMonitorIcon(
                    planType = planType,
                    monitorStatus = monitorStatus
                )
            }
        )

        BottomNavigationItem(
            selected = selection == BottomBarSelection.Profile,
            selectedContentColor = PassTheme.colors.interactionNormMajor2,
            unselectedContentColor = PassTheme.colors.textNorm,
            onClick = { onEvent(HomeBottomBarEvent.OnProfileSelected) },
            icon = {
                ProfileBottomBarIcon(planType = planType)
            },
            modifier = Modifier.padding(bottom = bottomPadding).testTag(BottomBarTestTag.PROFILE_TEST_TAG)
        )
    }
}

object BottomBarTestTag {
    const val PROFILE_TEST_TAG = "profile"
}

@[Preview Composable]
fun HomeBottomBarContentPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            HomeBottomBarContent(
                selection = BottomBarSelection.Home,
                onEvent = {},
                state = HomeBottomBarState(
                    planType = PlanType.Unknown(),
                    monitorStatus = MonitorStatusPreference.NoIssues
                )
            )
        }
    }
}

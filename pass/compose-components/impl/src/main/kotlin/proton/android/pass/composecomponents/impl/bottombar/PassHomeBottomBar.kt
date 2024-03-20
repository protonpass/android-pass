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

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.core.presentation.R
import proton.android.pass.commonpresentation.api.bars.bottom.home.presentation.HomeBottomBarEvent
import proton.android.pass.commonpresentation.api.bars.bottom.home.presentation.HomeBottomBarSelection
import proton.android.pass.commonpresentation.api.bars.bottom.home.presentation.HomeBottomBarState
import proton.android.pass.commonpresentation.api.bars.bottom.home.presentation.HomeBottomBarViewModel
import proton.android.pass.commonpresentation.impl.bars.bottom.home.presentation.HomeBottomBarViewModelImpl
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.domain.PlanType
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
fun PassHomeBottomBar(
    modifier: Modifier = Modifier,
    selection: HomeBottomBarSelection,
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
    selection: HomeBottomBarSelection,
    onEvent: (HomeBottomBarEvent) -> Unit,
    state: HomeBottomBarState
) = with(state) {
    BottomNavigation(
        modifier = modifier,
        backgroundColor = PassTheme.colors.bottomBarBackground
    ) {
        BottomNavigationItem(
            selected = selection == HomeBottomBarSelection.Home,
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
            selected = false,
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

        if (isSecurityCenterEnabled) {
            BottomNavigationItem(
                selected = selection == HomeBottomBarSelection.SecurityCenter,
                selectedContentColor = PassTheme.colors.interactionNormMajor2,
                unselectedContentColor = PassTheme.colors.textNorm,
                onClick = { onEvent(HomeBottomBarEvent.OnSecurityCenterSelected) },
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_proton_shield_half_filled),
                        contentDescription = stringResource(
                            id = CompR.string.bottom_bar_security_center_icon_content_description
                        )
                    )
                }
            )
        }

        BottomNavigationItem(
            selected = selection == HomeBottomBarSelection.Profile,
            selectedContentColor = PassTheme.colors.interactionNormMajor2,
            unselectedContentColor = PassTheme.colors.textNorm,
            onClick = { onEvent(HomeBottomBarEvent.OnProfileSelected) },
            icon = {
                ProfileBottomBarIcon(planType = planType)
            }
        )
    }
}

object BottomBarTestTag {
    const val profile = "profile"
}

@[Preview Composable]
fun HomeBottomBarContentPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            HomeBottomBarContent(
                selection = HomeBottomBarSelection.Home,
                onEvent = {},
                state = HomeBottomBarState(
                    planType = PlanType.Unknown(),
                    isSecurityCenterEnabled = true
                )
            )
        }
    }
}

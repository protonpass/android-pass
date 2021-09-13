/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */
package me.proton.core.compose.component

import androidx.compose.foundation.layout.Box
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import me.proton.core.compose.theme.ProtonBottomNavigationTheme

data class NavigationTab(
    val iconResId: Int,
    val titleResId: Int
)

@Composable
fun BottomNavigation(
    selectedTab: NavigationTab,
    tabs: List<NavigationTab>,
    onSelectedTab: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    ProtonBottomNavigationTheme {
        Box(modifier) {
            BottomNavigation(
                backgroundColor = MaterialTheme.colors.background
            ) {
                tabs.forEach { tab ->
                    val isSelected = selectedTab == tab
                    ProtonBottomNavigationTheme(isSelected) {
                        BottomNavigationItem(
                            icon = {
                                Icon(
                                    painter = painterResource(id = tab.iconResId),
                                    contentDescription = stringResource(id = tab.titleResId)
                                )
                            },
                            label = { Text(stringResource(id = tab.titleResId)) },
                            selected = isSelected,
                            onClick = { onSelectedTab(tab) },
                            selectedContentColor = MaterialTheme.colors.primary,
                            unselectedContentColor = MaterialTheme.colors.onBackground
                        )
                    }
                }
            }
            Divider(color = MaterialTheme.colors.surface)
        }
    }
}

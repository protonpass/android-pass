/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.composecomponents.impl.topbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.presentation.R
import proton.android.pass.commonui.api.PassPalette
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.composecomponents.impl.icon.VaultIcon

@Composable
fun SearchTopBar(
    modifier: Modifier = Modifier,
    searchQuery: String,
    placeholderText: String,
    inSearchMode: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onEnterSearch: () -> Unit,
    onStopSearch: () -> Unit,
    drawerIcon: @Composable () -> Unit,
    actions: (@Composable () -> Unit)? = null
) {
    val endPadding = if (actions != null) 4.dp else 16.dp
    Row(
        modifier = modifier.padding(start = 16.dp, top = 16.dp, end = endPadding, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        drawerIcon()
        SearchTextField(
            modifier = Modifier.weight(1f),
            searchQuery = searchQuery,
            placeholderText = placeholderText,
            inSearchMode = inSearchMode,
            onSearchQueryChange = onSearchQueryChange,
            onEnterSearch = onEnterSearch,
            onStopSearch = onStopSearch,
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            painterResource(R.drawable.ic_proton_cross),
                            contentDescription = null,
                            tint = ProtonTheme.colors.iconWeak
                        )
                    }
                }
            } else null
        )
        actions?.invoke()
    }
}

@Preview
@Composable
fun SearchTopBarPreview(@PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>) {
    PassTheme(isDark = input.first) {
        Surface {
            SearchTopBar(
                searchQuery = "",
                placeholderText = "Search in all vaults",
                onSearchQueryChange = { },
                drawerIcon = {
                    VaultIcon(
                        backgroundColor = PassPalette.MacaroniAndCheese16,
                        iconColor = PassPalette.MacaroniAndCheese100,
                        icon = R.drawable.ic_proton_house
                    )
                },
                inSearchMode = true,
                onStopSearch = {},
                onEnterSearch = {},
                actions = {
                    if (input.second) {
                        IconButton(onClick = {}) {
                            Icon(
                                painter = painterResource(
                                    id = R.drawable.ic_proton_three_dots_vertical
                                ),
                                contentDescription = null,
                                tint = PassTheme.colors.textWeak
                            )
                        }
                    }
                }
            )
        }
    }
}

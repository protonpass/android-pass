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

package proton.android.pass.featuresharing.impl.sharingwith

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Checkbox
import androidx.compose.material.Surface
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import me.proton.core.compose.theme.defaultWeak
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.commonui.api.body3Norm
import proton.android.pass.composecomponents.impl.container.CircleTextIcon
import proton.android.pass.featuresharing.impl.R

@Composable
fun InviteSuggestions(
    modifier: Modifier = Modifier,
    recentItems: List<Pair<String, Boolean>>,
    groupItems: List<Pair<String, Boolean>>,
    onItemClicked: (String) -> Unit
) {
    Column(
        modifier = modifier.padding(horizontal = Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        Text(
            style = ProtonTheme.typography.defaultWeak,
            text = stringResource(id = R.string.share_with_suggestions_title)
        )
        var selectedIndex by remember { mutableStateOf(0) }
        TabRow(
            selectedTabIndex = selectedIndex,
            backgroundColor = PassTheme.colors.interactionNormMinor1,
            modifier = Modifier
                .clip(CircleShape),
            indicator = { },
            divider = { }
        ) {
            InviteSuggestionTabs.values().forEachIndexed { index, tab ->
                val selected = selectedIndex == index
                Tab(
                    modifier = Modifier
                        .padding(Spacing.extraSmall)
                        .clip(CircleShape)
                        .applyIf(
                            condition = selected,
                            ifTrue = { background(PassTheme.colors.interactionNorm) },
                        ),
                    text = {
                        val title = when (tab) {
                            InviteSuggestionTabs.Recents -> stringResource(id = R.string.share_with_recents_title)
                            InviteSuggestionTabs.GroupSuggestions -> "Family" // this has to come from BE
                        }
                        Text(
                            text = title,
                            style = PassTheme.typography.body3Norm()
                        )
                    },
                    selected = selected,
                    onClick = { selectedIndex = index }
                )
            }
        }
        when (InviteSuggestionTabs.values()[selectedIndex]) {
            InviteSuggestionTabs.Recents -> InviteSuggestionList(
                items = recentItems,
                onItemClicked = onItemClicked
            )

            InviteSuggestionTabs.GroupSuggestions -> InviteSuggestionList(
                items = groupItems,
                onItemClicked = onItemClicked
            )
        }
    }
}

@Composable
fun InviteSuggestionList(
    modifier: Modifier = Modifier,
    items: List<Pair<String, Boolean>>,
    onItemClicked: (String) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        content = {
            items(items = items, key = { it.first }) { (email, isChecked) ->
                Row(
                    modifier = Modifier.clickable { onItemClicked(email) },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
                ) {
                    CircleTextIcon(
                        text = email,
                        backgroundColor = PassTheme.colors.interactionNormMinor1,
                        textColor = PassTheme.colors.interactionNormMajor2,
                        shape = PassTheme.shapes.squircleMediumShape
                    )
                    Text(
                        modifier = Modifier.weight(1f),
                        text = email,
                        style = ProtonTheme.typography.defaultNorm
                    )
                    Checkbox(checked = isChecked, onCheckedChange = { })
                }
            }
        }
    )
}

enum class InviteSuggestionTabs {
    Recents,
    GroupSuggestions
}

@Preview
@Composable
fun InviteSuggestionsPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            InviteSuggestions(
                recentItems = listOf("test1@proton.me" to true, "test2@proton.me" to false),
                groupItems = listOf(),
                onItemClicked = {}
            )
        }
    }
}

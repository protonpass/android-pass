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

package proton.android.pass.featureselectitem.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonui.api.body3Bold
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.container.InfoBanner
import proton.android.pass.composecomponents.impl.item.ActionableItemRow
import proton.android.pass.featureselectitem.R
import proton.android.pass.featureselectitem.previewproviders.SuggestionsInput
import proton.android.pass.featureselectitem.previewproviders.SuggestionsPreviewProvider
import proton.android.pass.composecomponents.impl.R as CompR

@Suppress("LongParameterList")
fun LazyListScope.SelectItemListHeader(
    suggestionsForTitle: String,
    suggestions: List<ItemUiModel>,
    canLoadExternalImages: Boolean,
    showUpgradeMessage: Boolean,
    canUpgrade: Boolean,
    onItemOptionsClicked: (ItemUiModel) -> Unit,
    onItemClicked: (ItemUiModel) -> Unit,
    onUpgradeClick: () -> Unit
) {

    if (showUpgradeMessage) {
        item {
            val text = buildAnnotatedString {
                append(stringResource(R.string.select_item_only_searching_in_oldest_vaults))
                append(' ')
                if (canUpgrade) {
                    withStyle(
                        style = SpanStyle(
                            textDecoration = TextDecoration.Underline,
                            color = PassTheme.colors.loginInteractionNormMajor2
                        )
                    ) {
                        append(stringResource(CompR.string.action_upgrade_now))
                    }
                }
            }

            InfoBanner(
                modifier = Modifier.padding(horizontal = Spacing.medium),
                backgroundColor = PassTheme.colors.interactionNormMinor1,
                text = text,
                onClick = if (canUpgrade) { onUpgradeClick } else null
            )
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }
    }

    if (suggestions.isEmpty()) return

    item {
        Text(
            modifier = Modifier.padding(start = Spacing.medium),
            text = stringResource(
                R.string.select_item_suggestions_for_placeholder,
                suggestionsForTitle
            ),
            style = PassTheme.typography.body3Bold()
        )
    }

    item { Spacer(modifier = Modifier.height(8.dp)) }

    // As items can appear in both lists, we need to use a different key here
    // so there are not two items with the same key
    items(items = suggestions, key = { "suggestion-${it.key}" }) { item ->
        ActionableItemRow(
            item = item,
            showMenuIcon = true,
            onItemClick = onItemClicked,
            onItemMenuClick = onItemOptionsClicked,
            canLoadExternalImages = canLoadExternalImages
        )
    }
}

internal class ThemedSuggestionsPreviewProvider :
    ThemePairPreviewProvider<SuggestionsInput>(SuggestionsPreviewProvider())

@Preview
@Composable
internal fun SelectItemListHeaderPreview(
    @PreviewParameter(ThemedSuggestionsPreviewProvider::class) input: Pair<Boolean, SuggestionsInput>
) {
    PassTheme(isDark = input.first) {
        Surface {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                SelectItemListHeader(
                    suggestionsForTitle = "some.website",
                    suggestions = input.second.items,
                    canLoadExternalImages = false,
                    showUpgradeMessage = input.second.showUpgradeMessage,
                    canUpgrade = input.second.canUpgrade,
                    onItemClicked = {},
                    onItemOptionsClicked = {},
                    onUpgradeClick = {}
                )
            }
        }
    }
}

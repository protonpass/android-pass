package proton.android.pass.autofill.ui.autofill.select

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
import proton.android.pass.autofill.service.R
import proton.android.pass.autofill.ui.previewproviders.SuggestionsInput
import proton.android.pass.autofill.ui.previewproviders.SuggestionsPreviewProvider
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.container.InfoBanner
import proton.android.pass.composecomponents.impl.item.ActionableItemRow
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
                append(stringResource(R.string.autofill_only_searching_in_primary_vault))
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
                modifier = Modifier.padding(horizontal = 16.dp),
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
            modifier = Modifier.padding(start = 16.dp),
            text = stringResource(
                R.string.autofill_suggestions_for_placeholder,
                suggestionsForTitle
            ),
            style = PassTypography.body3Bold
        )
    }

    item { Spacer(modifier = Modifier.height(8.dp)) }

    // As items can appear in both lists, we need to use a different key here
    // so there are not two items with the same key
    items(items = suggestions, key = { "suggestion-${it.id.id}" }) { item ->
        ActionableItemRow(
            item = item,
            showMenuIcon = true,
            onItemClick = onItemClicked,
            onItemMenuClick = onItemOptionsClicked,
            canLoadExternalImages = canLoadExternalImages
        )
    }
}

class ThemedSuggestionsPreviewProvider :
    ThemePairPreviewProvider<SuggestionsInput>(SuggestionsPreviewProvider())

@Preview
@Composable
fun SelectItemListHeaderPreview(
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

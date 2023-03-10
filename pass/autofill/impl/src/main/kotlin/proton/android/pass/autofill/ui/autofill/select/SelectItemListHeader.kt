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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallWeak
import proton.android.pass.autofill.service.R
import proton.android.pass.autofill.ui.previewproviders.SuggestionsPreviewProvider
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.item.ActionableItemRow

fun LazyListScope.SelectItemListHeader(
    suggestionsForTitle: String,
    suggestions: List<ItemUiModel>,
    onItemClicked: (ItemUiModel) -> Unit
) {
    if (suggestions.isEmpty()) return

    item {
        Text(
            modifier = Modifier.padding(start = 16.dp),
            text = stringResource(
                R.string.autofill_suggestions_for_placeholder,
                suggestionsForTitle
            ),
            style = ProtonTheme.typography.defaultSmallWeak
        )
    }

    item { Spacer(modifier = Modifier.height(8.dp)) }

    // As items can appear in both lists, we need to use a different key here
    // so there are not two items with the same key
    items(items = suggestions, key = { "suggestion-${it.id.id}" }) { item ->
        ActionableItemRow(
            item = item,
            showMenuIcon = false,
            onItemClick = onItemClicked
        )
    }

    item { Spacer(modifier = Modifier.height(24.dp)) }

    item {
        Text(
            modifier = Modifier.padding(start = 16.dp),
            text = stringResource(R.string.autofill_suggestions_other_items),
            style = ProtonTheme.typography.defaultSmallWeak
        )
    }
}

class ThemedSuggestionsPreviewProvider :
    ThemePairPreviewProvider<List<ItemUiModel>>(SuggestionsPreviewProvider())

@Preview
@Composable
fun SelectItemListHeaderPreview(
    @PreviewParameter(ThemedSuggestionsPreviewProvider::class) input: Pair<Boolean, List<ItemUiModel>>
) {
    PassTheme(isDark = input.first) {
        Surface {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                SelectItemListHeader(
                    suggestionsForTitle = "some.website",
                    suggestions = input.second,
                    onItemClicked = {}
                )
            }
        }
    }
}

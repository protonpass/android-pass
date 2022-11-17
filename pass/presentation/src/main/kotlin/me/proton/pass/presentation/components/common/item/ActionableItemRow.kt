package me.proton.pass.presentation.components.common.item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.commonui.api.ThemePairPreviewProvider
import me.proton.pass.presentation.components.model.ItemUiModel
import me.proton.pass.presentation.components.previewproviders.ItemUiModelPreviewProvider

@Composable
internal fun ActionableItemRow(
    modifier: Modifier = Modifier,
    item: ItemUiModel,
    highlight: String? = null,
    itemActions: List<ItemAction> = emptyList(),
    onItemClicked: (ItemUiModel) -> Unit = {}
) {
    val (expanded, setExpanded) = remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onItemClicked(item) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ItemRowContents(
            item = item,
            highlight = highlight,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        ItemRowActions(
            expanded = expanded,
            setExpanded = setExpanded,
            actions = itemActions,
            item = item
        )
    }
}

class ThemeAndItemUiModelProvider :
    ThemePairPreviewProvider<ItemUiModel>(ItemUiModelPreviewProvider())

@Preview
@Composable
fun ActionableItemRowPreview(
    @PreviewParameter(ThemeAndItemUiModelProvider::class) input: Pair<Boolean, ItemUiModel>
) {
    ProtonTheme(isDark = input.first) {
        Surface {
            ActionableItemRow(item = input.second)
        }
    }
}

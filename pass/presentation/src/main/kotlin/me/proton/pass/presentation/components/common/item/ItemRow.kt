package me.proton.pass.presentation.components.common.item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.common.api.None
import me.proton.pass.common.api.Option
import me.proton.pass.commonui.api.ThemePairPreviewProvider
import me.proton.pass.presentation.components.model.ItemUiModel
import me.proton.pass.presentation.components.previewproviders.ItemUiModelPreviewProvider

@Composable
internal fun ItemRow(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    title: AnnotatedString,
    subtitle: AnnotatedString
) {
    Row(
        modifier = modifier
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        icon()
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Row {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W400,
                    color = ProtonTheme.colors.textNorm,
                    maxLines = 1
                )
            }
            Row {
                Text(
                    text = subtitle,
                    color = ProtonTheme.colors.textWeak,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W400,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
internal fun ItemRow(
    modifier: Modifier = Modifier,
    item: ItemUiModel,
    highlight: Option<String> = None,
    itemActions: List<ItemAction> = emptyList(),
    onItemClicked: (ItemUiModel) -> Unit = {}
) {
    val (expanded, setExpanded) = remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onItemClicked.invoke(item) }
            .padding(end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ItemRowContents(
            item = item,
            highlight = highlight,
            modifier = Modifier.weight(1f)
        )
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
fun ItemRowPreview(
    @PreviewParameter(ThemeAndItemUiModelProvider::class) input: Pair<Boolean, ItemUiModel>
) {
    ProtonTheme(isDark = input.first) {
        Surface {
            ItemRow(item = input.second)
        }
    }
}

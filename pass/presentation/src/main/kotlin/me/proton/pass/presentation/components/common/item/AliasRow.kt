package me.proton.pass.presentation.components.common.item

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.common.api.None
import me.proton.pass.common.api.Option
import me.proton.pass.commonui.api.ThemePairPreviewProvider
import me.proton.pass.domain.ItemType
import me.proton.pass.presentation.components.common.item.icon.AliasIcon
import me.proton.pass.presentation.components.model.ItemUiModel
import me.proton.pass.presentation.components.previewproviders.AliasItemParameter
import me.proton.pass.presentation.components.previewproviders.AliasItemPreviewProvider

@Composable
internal fun AliasRow(
    modifier: Modifier = Modifier,
    item: ItemUiModel,
    itemType: ItemType.Alias,
    highlight: Option<String>
) {
    ItemRow(
        icon = { AliasIcon() },
        title = item.name.highlight(highlight),
        subtitle = itemType.aliasEmail.highlight(highlight),
        modifier = modifier
    )
}

class ThemedAliasItemPreviewProvider : ThemePairPreviewProvider<AliasItemParameter>(
    AliasItemPreviewProvider()
)

@Preview
@Composable
fun AliasRowPreview(
    @PreviewParameter(ThemedAliasItemPreviewProvider::class) input: Pair<Boolean, AliasItemParameter>
) {
    ProtonTheme(isDark = input.first) {
        Surface {
            AliasRow(
                item = input.second.model,
                highlight = None,
                itemType = input.second.itemType
            )
        }
    }
}

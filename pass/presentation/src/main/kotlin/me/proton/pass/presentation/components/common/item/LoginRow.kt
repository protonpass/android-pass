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
import me.proton.pass.presentation.components.common.item.icon.LoginIcon
import me.proton.pass.presentation.components.model.ItemUiModel
import me.proton.pass.presentation.components.previewproviders.LoginItemParameter
import me.proton.pass.presentation.components.previewproviders.LoginItemPreviewProvider

@Composable
fun LoginRow(
    modifier: Modifier = Modifier,
    item: ItemUiModel,
    itemType: ItemType.Login,
    highlight: Option<String>
) {
    ItemRow(
        icon = { LoginIcon() },
        title = item.name.highlight(highlight),
        subtitle = itemType.username.highlight(highlight),
        modifier = modifier
    )
}

class ThemedLoginItemPreviewProvider : ThemePairPreviewProvider<LoginItemParameter>(
    LoginItemPreviewProvider()
)

@Preview
@Composable
fun LoginRowPreview(
    @PreviewParameter(ThemedLoginItemPreviewProvider::class) input: Pair<Boolean, LoginItemParameter>
) {
    ProtonTheme(isDark = input.first) {
        Surface {
            LoginRow(
                item = input.second.model,
                highlight = None,
                itemType = input.second.itemType
            )
        }
    }
}

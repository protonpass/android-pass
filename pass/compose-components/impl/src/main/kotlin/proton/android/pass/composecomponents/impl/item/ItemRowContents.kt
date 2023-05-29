package proton.android.pass.composecomponents.impl.item

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.pass.domain.ItemContents

@Composable
internal fun ItemRowContents(
    modifier: Modifier = Modifier,
    item: ItemUiModel,
    highlight: String,
    vaultIcon: Int? = null,
    canLoadExternalImages: Boolean,
) {
    when (item.contents) {
        is ItemContents.Login -> LoginRow(
            modifier = modifier,
            item = item,
            highlight = highlight,
            vaultIcon = vaultIcon,
            canLoadExternalImages = canLoadExternalImages
        )

        is ItemContents.Note -> NoteRow(
            modifier = modifier,
            item = item,
            highlight = highlight,
            vaultIcon = vaultIcon
        )

        is ItemContents.Alias -> AliasRow(
            modifier = modifier,
            item = item,
            highlight = highlight,
            vaultIcon = vaultIcon
        )

        is ItemContents.Unknown -> {
        }
    }
}

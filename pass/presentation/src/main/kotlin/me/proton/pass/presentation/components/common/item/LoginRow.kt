package me.proton.pass.presentation.components.common.item

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.commonui.api.ThemePairPreviewProvider
import me.proton.pass.domain.ItemType
import me.proton.pass.presentation.components.common.item.icon.LoginIcon
import me.proton.pass.presentation.components.model.ItemUiModel
import me.proton.pass.presentation.components.previewproviders.LoginRowParameter
import me.proton.pass.presentation.components.previewproviders.LoginRowPreviewProvider

@Composable
fun LoginRow(
    modifier: Modifier = Modifier,
    item: ItemUiModel,
    highlight: String = ""
) {
    require(item.itemType is ItemType.Login)
    var title = AnnotatedString(item.name)
    var username = AnnotatedString(item.itemType.username)
    var note: AnnotatedString? = null
    val websites: MutableList<AnnotatedString> = mutableListOf()
    if (highlight.isNotBlank()) {
        val regex = highlight.toRegex(setOf(RegexOption.IGNORE_CASE))
        val titleMatches = regex.findAll(item.name)
        if (titleMatches.any()) {
            title = item.name.highlight(titleMatches)
        }
        val usernameMatches = regex.findAll(item.itemType.username)
        if (usernameMatches.any()) {
            username = item.itemType.username.highlight(usernameMatches)
        }
        val noteMatches = regex.findAll(item.note)
        if (noteMatches.any()) {
            note = item.note.highlight(noteMatches)
        }
        item.itemType.websites.forEach {
            val websiteMatch = regex.findAll(it)
            if (websiteMatch.any()) {
                websites.add(it.highlight(websiteMatch))
            }
            if (websites.size >= 2) return@forEach
        }
    }

    ItemRow(
        icon = { LoginIcon() },
        title = title,
        subtitles = listOfNotNull(username, note) + websites,
        modifier = modifier
    )
}

class ThemedLoginItemPreviewProvider : ThemePairPreviewProvider<LoginRowParameter>(
    LoginRowPreviewProvider()
)

@Preview
@Composable
fun LoginRowPreview(
    @PreviewParameter(ThemedLoginItemPreviewProvider::class) input: Pair<Boolean, LoginRowParameter>
) {
    ProtonTheme(isDark = input.first) {
        Surface {
            LoginRow(
                item = input.second.model,
                highlight = input.second.highlight
            )
        }
    }
}

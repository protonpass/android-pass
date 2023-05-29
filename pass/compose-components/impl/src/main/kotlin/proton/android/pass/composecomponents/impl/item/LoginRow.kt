package proton.android.pass.composecomponents.impl.item

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.toImmutableList
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.item.icon.LoginIcon
import proton.pass.domain.ItemContents

@Composable
fun LoginRow(
    modifier: Modifier = Modifier,
    item: ItemUiModel,
    highlight: String = "",
    vaultIcon: Int? = null,
    canLoadExternalImages: Boolean,
) {
    val content = item.contents as ItemContents.Login
    var title = AnnotatedString(content.title)
    var username = AnnotatedString(content.username)
    var note: AnnotatedString? = null
    val websites: MutableList<AnnotatedString> = mutableListOf()
    if (highlight.isNotBlank()) {
        val regex = highlight.toRegex(setOf(RegexOption.IGNORE_CASE, RegexOption.LITERAL))
        val titleMatches = regex.findAll(content.title)
        if (titleMatches.any()) {
            title = content.title.highlight(titleMatches)
        }
        val usernameMatches = regex.findAll(content.username)
        if (usernameMatches.any()) {
            username = content.username.highlight(usernameMatches)
        }
        val cleanNote = content.note.replace("\n", " ")
        val noteMatches = regex.findAll(cleanNote)
        if (noteMatches.any()) {
            note = cleanNote.highlight(noteMatches)
        }
        content.urls.forEach {
            val websiteMatch = regex.findAll(it)
            if (websiteMatch.any()) {
                websites.add(it.highlight(websiteMatch))
            }
            if (websites.size >= 2) return@forEach
        }
    }

    ItemRow(
        modifier = modifier,
        icon = {
            LoginIcon(
                text = title.text,
                content = content,
                canLoadExternalImages = canLoadExternalImages
            )
        },
        title = title,
        subtitles = (listOfNotNull(username, note) + websites).toImmutableList(),
        vaultIcon = vaultIcon
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
    PassTheme(isDark = input.first) {
        Surface {
            LoginRow(
                item = input.second.model,
                highlight = input.second.highlight,
                canLoadExternalImages = false
            )
        }
    }
}

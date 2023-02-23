package proton.android.pass.composecomponents.impl.item

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import coil.compose.AsyncImage
import kotlinx.collections.immutable.toImmutableList
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.container.Circle
import proton.android.pass.composecomponents.impl.item.icon.LoginIcon
import proton.pass.domain.ItemType

@Composable
fun LoginRow(
    modifier: Modifier = Modifier,
    item: ItemUiModel,
    highlight: String = ""
) {
    with(item.itemType as ItemType.Login) {
        var title = AnnotatedString(item.name)
        var username = AnnotatedString(this.username)
        var note: AnnotatedString? = null
        val websites: MutableList<AnnotatedString> = mutableListOf()
        if (highlight.isNotBlank()) {
            val regex = highlight.toRegex(setOf(RegexOption.IGNORE_CASE))
            val titleMatches = regex.findAll(item.name)
            if (titleMatches.any()) {
                title = item.name.highlight(titleMatches)
            }
            val usernameMatches = regex.findAll(this.username)
            if (usernameMatches.any()) {
                username = this.username.highlight(usernameMatches)
            }
            val cleanNote = item.note.replace("\n", " ")
            val noteMatches = regex.findAll(cleanNote)
            if (noteMatches.any()) {
                note = cleanNote.highlight(noteMatches)
            }
            this.websites.forEach {
                val websiteMatch = regex.findAll(it)
                if (websiteMatch.any()) {
                    websites.add(it.highlight(websiteMatch))
                }
                if (websites.size >= 2) return@forEach
            }
        }

        ItemRow(
            icon = { LoginRowIcon(title = title.text, item = this) },
            title = title,
            subtitles = (listOfNotNull(username, note) + websites).toImmutableList(),
            modifier = modifier
        )
    }
}

@Composable
fun LoginRowIcon(
    modifier: Modifier = Modifier,
    title: String,
    item: ItemType.Login
) {
    val firstWebsite = item.websites.firstOrNull()
    if (firstWebsite == null) {
        LoginIcon(modifier = modifier, text = title)
    } else {
        Circle(
            modifier = modifier,
            backgroundColor = Color.Transparent
        ) {
            AsyncImage(
                modifier = Modifier.clip(CircleShape),
                model = firstWebsite,
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )
        }
    }
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
                highlight = input.second.highlight
            )
        }
    }
}

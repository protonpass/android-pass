package proton.android.pass.composecomponents.impl.item

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.item.icon.CreditCardIcon
import proton.pass.domain.ItemContents

@Composable
fun CreditCardRow(
    modifier: Modifier = Modifier,
    item: ItemUiModel,
    highlight: String = "",
    vaultIcon: Int? = null
) {
    val content = item.contents as ItemContents.CreditCard
    var title = AnnotatedString(content.title)
    val maskedNumber = remember(content.number) {
        val start = content.number.take(4)
        val end = content.number.takeLast(4)
        AnnotatedString("$start •••• •••• $end")
    }
    var note: AnnotatedString? = null
    if (highlight.isNotBlank()) {
        val regex = highlight.toRegex(setOf(RegexOption.IGNORE_CASE, RegexOption.LITERAL))
        val titleMatches = regex.findAll(content.title)
        if (titleMatches.any()) {
            title = content.title.highlight(titleMatches)
        }
        val cleanNote = content.note.replace("\n", " ")
        val noteMatches = regex.findAll(cleanNote)
        if (noteMatches.any()) {
            note = cleanNote.highlight(noteMatches)
        }
    }

    ItemRow(
        modifier = modifier,
        icon = { CreditCardIcon() },
        title = title,
        subtitles = listOfNotNull(maskedNumber, note).toPersistentList(),
        vaultIcon = vaultIcon
    )
}

class ThemedCreditCardPreviewProvider : ThemePairPreviewProvider<CreditCardRowParameter>(
    CreditCardRowPreviewProvider()
)

@Preview
@Composable
fun CreditCardRowPreview(
    @PreviewParameter(ThemedCreditCardPreviewProvider::class) input: Pair<Boolean, CreditCardRowParameter>
) {
    PassTheme(isDark = input.first) {
        Surface {
            CreditCardRow(
                item = input.second.model,
                highlight = input.second.highlight,
            )
        }
    }
}

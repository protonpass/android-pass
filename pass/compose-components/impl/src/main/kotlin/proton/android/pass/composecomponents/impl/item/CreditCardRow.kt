package proton.android.pass.composecomponents.impl.item

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    val maskedNumber = remember(content.number) {
        if (content.number.isBlank()) {
            null
        } else {
            val start = content.number.take(4)
            val end = content.number.takeLast(4)
            AnnotatedString("$start •••• •••• $end")
        }
    }

    val highlightColor = PassTheme.colors.interactionNorm
    val fields = remember(content.title, content.note, content.cardHolder, highlight) {
        getHighlightedFields(
            title = content.title,
            note = content.note,
            cardHolder = content.cardHolder,
            highlight = highlight,
            highlightColor = highlightColor
        )
    }

    ItemRow(
        modifier = modifier,
        icon = { CreditCardIcon() },
        title = fields.title,
        subtitles = listOfNotNull(maskedNumber, fields.note, fields.cardHolder).toPersistentList(),
        vaultIcon = vaultIcon
    )
}

private fun getHighlightedFields(
    title: String,
    note: String,
    cardHolder: String,
    highlight: String,
    highlightColor: Color
): CreditCardHighlightFields {
    var annotatedTitle = AnnotatedString(title)
    var annotatedNote: AnnotatedString? = null
    var annotatedCardHolder: AnnotatedString? = null
    if (highlight.isNotBlank()) {
        val regex = highlight.toRegex(setOf(RegexOption.IGNORE_CASE, RegexOption.LITERAL))
        val titleMatches = regex.findAll(title)
        if (titleMatches.any()) {
            annotatedTitle = title.highlight(titleMatches, highlightColor)
        }
        annotatedNote = highlightIfNeeded(regex, note, highlightColor)
        annotatedCardHolder = highlightIfNeeded(regex, cardHolder, highlightColor)
    }

    return CreditCardHighlightFields(
        title = annotatedTitle,
        note = annotatedNote,
        cardHolder = annotatedCardHolder
    )
}

@Stable
private data class CreditCardHighlightFields(
    val title: AnnotatedString,
    val note: AnnotatedString?,
    val cardHolder: AnnotatedString?
)

private fun highlightIfNeeded(
    regex: Regex,
    field: String,
    highlightColor: Color
): AnnotatedString? {
    val cleanField = field.replace("\n", " ")
    val matches = regex.findAll(cleanField)
    return if (matches.any()) cleanField.highlight(matches, highlightColor) else null
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

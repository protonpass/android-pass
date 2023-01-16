package proton.android.pass.composecomponents.impl.item

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.datetime.Clock
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.pass.domain.ItemId
import proton.pass.domain.ItemType
import proton.pass.domain.ShareId

class LoginRowPreviewProvider : PreviewParameterProvider<LoginRowParameter> {
    override val values: Sequence<LoginRowParameter>
        get() = sequenceOf(
            with(title = "Empty username", username = ""),
            with(title = "This is a login item", username = "the username"),
            with(
                title = "Very long text",
                username = "this is a very long username that should become " +
                    "ellipsized if the text does not fit properly"
            ),
            with(
                title = "Very long text",
                username = "this is a very long username that should " +
                    "highlight the word proton during the rendering",
                note = "this is a very long note that should " +
                    "highlight the word proton during the rendering",
                websites = listOf(
                    "https://somerandomwebsite.com/",
                    "https://proton.ch/",
                    "https://proton.me/",
                    "https://anotherrandomwebsite.com/"
                ),
                highlight = "proton"
            ),
            with(
                title = "With multiline content to check highlight",
                username = "username",
                note = "A note \n with \n multiline \n text \n to \n verify \n that" +
                    " the \n word \n local \n is highlighted",
                highlight = "local"
            )
        )

    companion object {
        private fun with(
            title: String,
            username: String,
            note: String = "Note content",
            websites: List<String> = emptyList(),
            highlight: String = ""
        ): LoginRowParameter =
            LoginRowParameter(
                model = ItemUiModel(
                    id = ItemId("123"),
                    shareId = ShareId("345"),
                    name = title,
                    note = note,
                    itemType = ItemType.Login(
                        username = username,
                        password = "",
                        websites = websites
                    ),
                    modificationTime = Clock.System.now()
                ),
                highlight = highlight
            )

    }
}

data class LoginRowParameter(
    val model: ItemUiModel,
    val highlight: String
)

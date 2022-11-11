package me.proton.pass.presentation.components.previewproviders

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ItemType
import me.proton.pass.domain.ShareId
import me.proton.pass.presentation.components.model.ItemUiModel

class LoginItemPreviewProvider : PreviewParameterProvider<LoginItemParameter> {
    override val values: Sequence<LoginItemParameter>
        get() = sequenceOf(
            with(title = "Empty username", username = ""),
            with(title = "This is a login item", username = "the username"),
            with(
                title = "Very long text",
                username = "this is a very long username that should become " +
                    "ellipsized if the text does not fit properly"
            )
        )

    companion object {
        private fun with(title: String, username: String): LoginItemParameter =
            LoginItemParameter(
                model = ItemUiModel(
                    id = ItemId("123"),
                    shareId = ShareId("345"),
                    name = title,
                    itemType = ItemType.Login(
                        username = username,
                        password = "",
                        websites = emptyList()
                    )
                ),
                itemType = ItemType.Login(username, "", emptyList())
            )

    }
}

data class LoginItemParameter(
    val model: ItemUiModel,
    val itemType: ItemType.Login
)

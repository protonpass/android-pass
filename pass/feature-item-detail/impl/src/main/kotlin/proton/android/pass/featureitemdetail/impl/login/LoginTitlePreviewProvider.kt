package proton.android.pass.featureitemdetail.impl.login

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.pass.domain.ShareColor
import proton.pass.domain.ShareIcon
import proton.pass.domain.ShareId
import proton.pass.domain.Vault

class LoginTitlePreviewProvider : PreviewParameterProvider<LoginTitleInput> {
    override val values: Sequence<LoginTitleInput>
        get() = sequence {
            val title = "A really long title to check if the element is multiline"
            yield(LoginTitleInput(title = title, vault = null))
            yield(
                LoginTitleInput(
                    title = "A really long title to check if the element is multiline",
                    vault = Vault(
                        shareId = ShareId("123"),
                        name = "A vault",
                        color = ShareColor.Color1,
                        icon = ShareIcon.Icon1,
                        isPrimary = false
                    )
                )
            )
        }
}

data class LoginTitleInput(
    val title: String,
    val vault: Vault?
)

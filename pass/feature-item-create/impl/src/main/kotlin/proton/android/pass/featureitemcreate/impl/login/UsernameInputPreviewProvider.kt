package proton.android.pass.featureitemcreate.impl.login

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class UsernameInputPreviewProvider : PreviewParameterProvider<UsernameInputPreview> {
    override val values: Sequence<UsernameInputPreview>
        get() = sequence {
            for (text in listOf("", "some value")) {
                for (isEditAllowed in listOf(false, true)) {
                    for (canUpdateUsername in listOf(false, true)) {
                        yield(UsernameInputPreview(text, isEditAllowed, canUpdateUsername))
                    }
                }
            }
        }
}

data class UsernameInputPreview(
    val text: String,
    val isEditAllowed: Boolean,
    val canUpdateUsername: Boolean
)

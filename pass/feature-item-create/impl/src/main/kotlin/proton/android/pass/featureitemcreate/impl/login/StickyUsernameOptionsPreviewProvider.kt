package proton.android.pass.featureitemcreate.impl.login

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption

class StickyUsernameOptionsPreviewProvider : PreviewParameterProvider<StickyUsernameInput> {
    override val values: Sequence<StickyUsernameInput>
        get() = sequenceOf(
            StickyUsernameInput("myemail@proton.me".toOption(), true),
            StickyUsernameInput("myemail@proton.me".toOption(), false),
            StickyUsernameInput(None, true),
        )
}

data class StickyUsernameInput(
    val primaryEmail: Option<String>,
    val showCreateAlias: Boolean
)

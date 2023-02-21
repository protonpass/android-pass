package proton.android.pass.featurecreateitem.impl.login

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption

class StickyUsernameOptionsPreviewProvider : PreviewParameterProvider<Option<String>> {
    override val values: Sequence<Option<String>>
        get() = sequenceOf("myemail@proton.me".toOption(), None)
}

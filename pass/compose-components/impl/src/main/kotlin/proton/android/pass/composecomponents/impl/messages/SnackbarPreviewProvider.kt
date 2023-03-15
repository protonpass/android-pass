package proton.android.pass.composecomponents.impl.messages

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import me.proton.core.compose.component.ProtonSnackbarType

class SnackbarTypePreviewProvider : PreviewParameterProvider<ProtonSnackbarType> {
    override val values: Sequence<ProtonSnackbarType>
        get() = sequence {
            ProtonSnackbarType.values().forEach { yield(it) }
        }
}


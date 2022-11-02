package me.proton.pass.presentation.components.previewproviders

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import me.proton.pass.presentation.uievents.IsButtonEnabled

class AliasTopBarPreviewProvider : PreviewParameterProvider<AliasTopBarInput> {
    override val values: Sequence<AliasTopBarInput>
        get() = sequenceOf(
            AliasTopBarInput(IsButtonEnabled.Enabled),
            AliasTopBarInput(IsButtonEnabled.Disabled)
        )
}

data class AliasTopBarInput(
    val buttonEnabled: IsButtonEnabled
)

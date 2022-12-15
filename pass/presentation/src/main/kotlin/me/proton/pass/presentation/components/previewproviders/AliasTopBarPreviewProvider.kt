package me.proton.pass.presentation.components.previewproviders

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import me.proton.pass.presentation.uievents.IsButtonEnabled

class AliasTopBarPreviewProvider : PreviewParameterProvider<AliasTopBarInput> {
    override val values: Sequence<AliasTopBarInput>
        get() = sequenceOf(
            AliasTopBarInput(
                false,
                IsButtonEnabled.Enabled
            ),
            AliasTopBarInput(
                false,
                IsButtonEnabled.Disabled
            ),
            AliasTopBarInput(
                true,
                IsButtonEnabled.Enabled
            ),
            AliasTopBarInput(
                true,
                IsButtonEnabled.Disabled
            )
        )
}

data class AliasTopBarInput(
    val isDraft: Boolean,
    val buttonEnabled: IsButtonEnabled
)

package me.proton.pass.presentation.components.previewproviders

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import me.proton.pass.presentation.uievents.IsButtonEnabled
import me.proton.pass.presentation.uievents.IsLoadingState

class AliasTopBarPreviewProvider : PreviewParameterProvider<AliasTopBarInput> {
    override val values: Sequence<AliasTopBarInput>
        get() = sequence {
            for (isDraft in listOf(false, true)) {
                for (isButtonEnabled in listOf(IsButtonEnabled.Enabled, IsButtonEnabled.Disabled)) {
                    yield(
                        AliasTopBarInput(
                            isDraft = isDraft,
                            buttonEnabled = isButtonEnabled,
                            isLoadingState = IsLoadingState.NotLoading
                        )
                    )
                }
            }
            yield(
                AliasTopBarInput(
                    isDraft = false,
                    buttonEnabled = IsButtonEnabled.Enabled,
                    isLoadingState = IsLoadingState.Loading
                )
            )
        }
}

data class AliasTopBarInput(
    val isDraft: Boolean,
    val buttonEnabled: IsButtonEnabled,
    val isLoadingState: IsLoadingState
)

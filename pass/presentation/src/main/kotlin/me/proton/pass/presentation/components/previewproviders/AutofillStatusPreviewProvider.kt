package me.proton.pass.presentation.components.previewproviders

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import me.proton.android.pass.autofill.api.AutofillStatus

class AutofillStatusPreviewProvider : PreviewParameterProvider<AutofillStatus> {
    override val values: Sequence<AutofillStatus>
        get() = sequenceOf(
            AutofillStatus.Disabled,
            AutofillStatus.EnabledByOurService,
            AutofillStatus.EnabledByOtherService
        )
}

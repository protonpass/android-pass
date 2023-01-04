package me.proton.android.pass.composecomponents.impl.bottomsheet

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

open class BottomSheetTitlePreviewProvider : PreviewParameterProvider<BottomSheetTitleButton?> {
    override val values: Sequence<BottomSheetTitleButton?> = sequenceOf(
        BottomSheetTitleButton(title = "Apply", onClick = {}, enabled = true),
        BottomSheetTitleButton(title = "Apply", onClick = {}, enabled = false),
        null
    )
}

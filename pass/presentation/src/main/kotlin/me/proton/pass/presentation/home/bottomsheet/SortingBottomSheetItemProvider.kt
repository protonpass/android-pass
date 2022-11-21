package me.proton.pass.presentation.home.bottomsheet

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class SortingBottomSheetItemProvider : PreviewParameterProvider<SortingBottomSheetItemParameter> {
    override val values: Sequence<SortingBottomSheetItemParameter>
        get() = sequenceOf(
            SortingBottomSheetItemParameter("Sort by name", true),
            SortingBottomSheetItemParameter("Sort by type", false)
        )
}

data class SortingBottomSheetItemParameter(
    val text: String,
    val isChecked: Boolean
)

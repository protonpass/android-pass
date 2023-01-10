package proton.android.pass.composecomponents.impl.form

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class ProtonFormInputPreviewProvider :
    PreviewParameterProvider<ProtonFormInputPreviewData> {
    override val values: Sequence<ProtonFormInputPreviewData>
        get() = sequenceOf(
            ProtonFormInputPreviewData(value = "", isRequired = false),
            ProtonFormInputPreviewData(
                value = "",
                isRequired = true,
                errorMessage = "Error",
                isError = true
            ),
            ProtonFormInputPreviewData(
                value = "required with error",
                isRequired = true,
                errorMessage = "Error",
                isError = true
            ),
            ProtonFormInputPreviewData(value = "not editable", isEditable = false)
        )
}

data class ProtonFormInputPreviewData(
    val value: String = "",
    val errorMessage: String = "",
    val isError: Boolean = false,
    val isRequired: Boolean = false,
    val isEditable: Boolean = true
)

package proton.android.pass.composecomponents.impl.form

import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.presentation.R

class ProtonTextFieldPreviewProvider : PreviewParameterProvider<ProtonTextFieldPreviewData> {
    override val values: Sequence<ProtonTextFieldPreviewData>
        get() = sequenceOf(
            ProtonTextFieldPreviewData(value = "", placeholder = ""),
            ProtonTextFieldPreviewData(
                value = "",
                placeholder = "Name"
            ),
            ProtonTextFieldPreviewData(value = "", isError = true, placeholder = ""),
            ProtonTextFieldPreviewData(
                value = "contents with error",
                isError = true,
                placeholder = ""
            ),
            ProtonTextFieldPreviewData(
                value = "not editable",
                isEditable = false,
                placeholder = ""
            ),
            ProtonTextFieldPreviewData(
                value = "with icon",
                icon = {
                    Icon(
                        painter = painterResource(
                            id = R.drawable.ic_proton_minus_circle
                        ),
                        contentDescription = null,
                        tint = ProtonTheme.colors.iconNorm
                    )
                },
                placeholder = ""
            )
        )
}

data class ProtonTextFieldPreviewData(
    val value: String = "",
    val placeholder: String,
    val isError: Boolean = false,
    val isEditable: Boolean = true,
    val icon: (@Composable () -> Unit)? = null
)

package proton.android.pass.composecomponents.impl.topbar.icon

import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi

@ExperimentalComposeUiApi
@Composable
fun ArrowBackIcon(
    onUpClick: () -> Unit
) {
    NavigationIcon(onUpClick = onUpClick) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = null
        )
    }
}

package proton.android.pass.composecomponents.impl.topbar.iconbutton

import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier

@ExperimentalComposeUiApi
@Composable
fun ArrowBackIconButton(
    modifier: Modifier = Modifier,
    onUpClick: () -> Unit
) {
    NavigationIconButton(
        modifier = modifier,
        onUpClick = onUpClick
    ) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = null
        )
    }
}

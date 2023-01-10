package proton.android.pass.composecomponents.impl.topbar.icon

import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.res.painterResource
import me.proton.core.presentation.R

@ExperimentalComposeUiApi
@Composable
fun ChevronBackIcon(
    onUpClick: () -> Unit
) {
    NavigationIcon(onUpClick = onUpClick) {
        Icon(
            painter = painterResource(R.drawable.ic_proton_chevron_left),
            contentDescription = null
        )
    }
}

package proton.android.pass.composecomponents.impl.topbar.iconbutton

import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import me.proton.core.presentation.R

@ExperimentalComposeUiApi
@Composable
fun HamburgerIconButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    NavigationIconButton(
        modifier = modifier,
        onUpClick = onClick
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_proton_hamburger),
            contentDescription = null
        )
    }
}

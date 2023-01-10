package proton.android.pass.composecomponents.impl.topbar.icon

import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import me.proton.core.presentation.R

@ExperimentalComposeUiApi
@Composable
fun HamburgerIcon(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    NavigationIcon(
        modifier = modifier,
        onUpClick = onClick
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_proton_hamburger),
            contentDescription = null
        )
    }
}

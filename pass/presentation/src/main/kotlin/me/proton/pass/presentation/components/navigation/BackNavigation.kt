package me.proton.android.pass.ui.shared

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource

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

@ExperimentalComposeUiApi
@Composable
fun ChevronBackIcon(
    onUpClick: () -> Unit
) {
    NavigationIcon(onUpClick = onUpClick) {
        Icon(
            painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_chevron_left),
            contentDescription = null
        )
    }
}

@ExperimentalComposeUiApi
@Composable
fun CrossBackIcon(
    onUpClick: () -> Unit
) {
    NavigationIcon(onUpClick = onUpClick) {
        Icon(
            painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_close),
            contentDescription = null
        )
    }
}

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
            painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_hamburger),
            contentDescription = null
        )
    }
}

@ExperimentalComposeUiApi
@Composable
private fun NavigationIcon(
    modifier: Modifier = Modifier,
    onUpClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    IconButton(
        modifier = modifier,
        onClick = {
            keyboardController?.hide()
            onUpClick()
        }
    ) {
        icon()
    }
}

package me.proton.android.pass.ui.shared

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import me.proton.pass.presentation.R

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
private fun NavigationIcon(
    onUpClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    IconButton(onClick = {
        keyboardController?.hide()
        onUpClick()
    }) {
        icon()
    }
}

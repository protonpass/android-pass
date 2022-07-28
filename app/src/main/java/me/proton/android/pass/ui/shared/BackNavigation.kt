package me.proton.android.pass.ui.shared

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import me.proton.android.pass.R

@ExperimentalComposeUiApi
@Composable
fun ArrowBackIcon(
    onUpClick: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    IconButton(onClick = {
        keyboardController?.hide()
        onUpClick()
    }) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = null
        )
    }
}

@ExperimentalComposeUiApi
@Composable
fun CrossBackIcon(
    onUpClick: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    IconButton(onClick = {
        keyboardController?.hide()
        onUpClick()
    }) {
        Icon(
            painter = painterResource(R.drawable.ic_proton_close),
            contentDescription = null
        )
    }
}

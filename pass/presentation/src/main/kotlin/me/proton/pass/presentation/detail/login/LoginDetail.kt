package me.proton.pass.presentation.detail.login

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.pass.domain.Item
import me.proton.pass.presentation.R

@Composable
fun LoginDetail(
    modifier: Modifier = Modifier,
    item: Item,
    viewModel: LoginDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(item) {
        viewModel.setItem(item)
    }

    val model by viewModel.viewState.collectAsStateWithLifecycle()

    val localContext = LocalContext.current
    val clipboard = LocalClipboardManager.current

    val copiedToClipboardSuffix = stringResource(R.string.field_copied_to_clipboard)
    val storeToClipboard = { contents: String?, fieldName: String ->
        if (contents != null) {
            clipboard.setText(AnnotatedString(contents))
            val message = "$fieldName $copiedToClipboardSuffix"
            Toast
                .makeText(localContext, message, Toast.LENGTH_SHORT)
                .show()
        }
    }

    val passwordFieldName = stringResource(R.string.field_password)
    LaunchedEffect(viewModel) {
        viewModel.copyToClipboardFlow.collect { storeToClipboard(it, passwordFieldName) }
    }

    LoginContent(
        modifier = modifier,
        model = model,
        onTogglePasswordClick = { viewModel.togglePassword() },
        onCopyPasswordClick = { viewModel.copyPasswordToClipboard() },
        storeToClipboard = storeToClipboard
    )
}

package me.proton.android.pass.ui.detail.login

import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import me.proton.android.pass.R
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.pass.domain.Item
import me.proton.core.pass.presentation.components.common.rememberFlowWithLifecycle

@Composable
fun LoginDetail(
    item: Item,
    modifier: Modifier = Modifier,
    viewModel: LoginDetailViewModel = hiltViewModel()
) {
    viewModel.setItem(item)

    val model by rememberFlowWithLifecycle(viewModel.viewState).collectAsState(initial = viewModel.initialViewState)

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

    LoginContentView(
        model = model,
        onTogglePasswordClick = { viewModel.togglePassword() },
        onCopyPasswordClick = { viewModel.copyPasswordToClipboard() },
        storeToClipboard = storeToClipboard,
        modifier = modifier
    )
}

@Composable
internal fun LoginContentView(
    model: LoginDetailViewModel.LoginUiModel,
    modifier: Modifier = Modifier,
    onTogglePasswordClick: () -> Unit,
    onCopyPasswordClick: () -> Unit,
    storeToClipboard: (contents: String?, fieldName: String) -> Unit
) {
    Column {
        Column(modifier = modifier.padding(horizontal = 16.dp)) {
            UsernameRow(
                model = model,
                storeToClipboard = storeToClipboard
            )
            WebsiteSection(model)
            PasswordRow(
                model = model,
                onTogglePasswordClick = onTogglePasswordClick,
                onCopyPasswordClick = onCopyPasswordClick
            )
            NoteRow(model)
        }
        Divider(
            modifier = Modifier.fillMaxWidth(),
            color = ProtonTheme.colors.separatorNorm
        )
    }
}

@Composable
internal fun UsernameRow(
    model: LoginDetailViewModel.LoginUiModel,
    storeToClipboard: (contents: String?, fieldName: String) -> Unit
) {
    val usernameFieldName = stringResource(R.string.field_username)
    Section(
        title = R.string.field_username_title,
        content = model.username,
        icon = R.drawable.ic_proton_squares,
        onIconClick = { storeToClipboard(model.username, usernameFieldName) }
    )
}

@Composable
internal fun WebsiteSection(
    model: LoginDetailViewModel.LoginUiModel
) {
    if (model.websites.isEmpty()) return

    Row(modifier = Modifier.padding(vertical = 12.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            SectionTitle(title = R.string.field_website_address_title)
            model.websites.forEach {
                Text(
                    text = it,
                    color = ProtonTheme.colors.textNorm,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
internal fun PasswordRow(
    model: LoginDetailViewModel.LoginUiModel,
    onTogglePasswordClick: () -> Unit,
    onCopyPasswordClick: () -> Unit
) {
    val actionContent = when (model.password) {
        is LoginDetailViewModel.PasswordState.Concealed -> stringResource(R.string.action_reveal_password)
        is LoginDetailViewModel.PasswordState.Revealed -> stringResource(R.string.action_conceal_password)
    }

    val sectionContent = when (val password = model.password) {
        is LoginDetailViewModel.PasswordState.Concealed -> "⬤".repeat(12)
        is LoginDetailViewModel.PasswordState.Revealed -> password.clearText
    }

    Section(
        title = R.string.field_detail_password_title,
        content = sectionContent,
        icon = R.drawable.ic_proton_squares,
        onIconClick = { onCopyPasswordClick() },
        viewBelow = {
            Text(
                text = actionContent,
                fontWeight = FontWeight.W400,
                fontSize = 12.sp,
                color = ProtonTheme.colors.brandNorm,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clickable { onTogglePasswordClick() }
            )
        }
    )
}

@Composable
internal fun NoteRow(
    model: LoginDetailViewModel.LoginUiModel
) {
    if (model.note.isNotEmpty()) {
        Section(
            title = R.string.field_note_title,
            content = model.note
        )
    }
}

@Composable
internal fun Section(
    @StringRes title: Int,
    @DrawableRes icon: Int? = null,
    content: String,
    contentTextColor: Color = ProtonTheme.colors.textWeak,
    onIconClick: (() -> Unit)? = null,
    viewBelow: @Composable (() -> Unit)? = null
) {
    Row(modifier = Modifier.padding(vertical = 12.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            SectionTitle(title)
            Text(
                text = content,
                color = contentTextColor,
                fontSize = 14.sp
            )

            if (viewBelow != null) {
                viewBelow()
            }
        }
        if (icon != null) {
            IconButton(
                onClick = { onIconClick?.invoke() },
                modifier = Modifier.then(Modifier.size(24.dp).align(Alignment.CenterVertically))
            ) {
                Icon(painter = painterResource(icon), contentDescription = null)
            }
        }
    }
}

@Composable
internal fun SectionTitle(
    @StringRes title: Int
) {
    Text(
        text = stringResource(title),
        fontSize = 16.sp,
        fontWeight = FontWeight.W400,
        color = ProtonTheme.colors.textNorm
    )
}

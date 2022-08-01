package me.proton.android.pass.ui.detail.login

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
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
    LoginContentView(model = model, modifier = modifier)
}

@Composable
internal fun LoginContentView(
    model: LoginDetailViewModel.LoginUiModel,
    modifier: Modifier = Modifier
) {
    Column {
        Column(modifier = modifier.padding(horizontal = 16.dp)) {
            UsernameRow(model)
            WebsiteRow(model)
            PasswordRow(model)
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
    model: LoginDetailViewModel.LoginUiModel
) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    Section(
        title = R.string.field_username_title,
        content = model.username,
        icon = R.drawable.ic_proton_squares,
        onIconClick = { clipboardManager.setText(AnnotatedString(model.username)) }
    )
}

@Composable
internal fun WebsiteRow(
    model: LoginDetailViewModel.LoginUiModel
) {
    if (model.websites.isNotEmpty()) {
        Section(
            title = R.string.field_website_address_title,
            content = model.websites[0],
        )
    }
}

@Composable
internal fun PasswordRow(
    model: LoginDetailViewModel.LoginUiModel
) {
    var valueHidden by remember { mutableStateOf(true) }
    val actionContent = when (valueHidden) {
        true -> stringResource(R.string.action_reveal_password)
        false -> stringResource(R.string.action_conceal_password)
    }

    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    Section(
        title = R.string.field_detail_password_title,
        content = model.password,
        icon = R.drawable.ic_proton_squares,
        hideValue = valueHidden,
        onIconClick = { clipboardManager.setText(AnnotatedString(model.password)) },
        viewBelow = {
            Text(
                text = actionContent,
                fontWeight = FontWeight.W400,
                fontSize = 12.sp,
                color = ProtonTheme.colors.brandNorm,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clickable { valueHidden = !valueHidden }
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
            content = model.note,
        )
    }
}

@Composable
internal fun Section(
    @StringRes title: Int,
    content: String,
    @DrawableRes icon: Int? = null,
    hideValue: Boolean = false,
    onIconClick: (() -> Unit)? = null,
    viewBelow: @Composable (() -> Unit)? = null,
) {
    Row(modifier = Modifier.padding(vertical = 12.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(title),
                fontSize = 16.sp,
                fontWeight = FontWeight.W400,
                color = ProtonTheme.colors.textNorm
            )
            if (hideValue) {
                Text(
                    text = "⬤".repeat(12),
                    fontSize = 14.sp,
                    color = ProtonTheme.colors.textNorm
                )
            } else {
                Text(
                    text = content,
                    fontSize = 14.sp,
                    color = ProtonTheme.colors.textWeak
                )
            }

            if (viewBelow != null) {
                viewBelow()
            }
        }
        if (icon != null) {
            IconButton(
                onClick = { onIconClick?.invoke() },
                modifier = Modifier.then(Modifier.size(24.dp))
            ) {
                Icon(painter = painterResource(icon), contentDescription = null)
            }
        }
    }
}

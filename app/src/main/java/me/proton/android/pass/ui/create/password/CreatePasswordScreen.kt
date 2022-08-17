package me.proton.android.pass.ui.create.password

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Slider
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.proton.android.pass.R
import me.proton.android.pass.ui.shared.ArrowBackIcon
import me.proton.android.pass.ui.shared.TopBarTitleView
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.pass.presentation.components.common.rememberFlowWithLifecycle

internal typealias OnConfirm = (String) -> Unit

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CreatePasswordView(
    @StringRes topBarTitle: Int,
    onUpClick: () -> Unit,
    onConfirm: OnConfirm
) {
    Scaffold(
        topBar = {
            ProtonTopAppBar(
                title = { TopBarTitleView(topBarTitle) },
                navigationIcon = { ArrowBackIcon(onUpClick = onUpClick) },
                actions = {},
            )
        }) { padding ->
        val viewModel by remember { mutableStateOf(CreatePasswordViewModel()) }
        CreatePasswordViewContent(
            modifier = Modifier.padding(padding),
            onUpClick = onUpClick,
            onConfirm = onConfirm,
            viewModel = viewModel,
        )
    }
}

@Composable
private fun CreatePasswordViewContent(
    modifier: Modifier,
    onUpClick: () -> Unit,
    onConfirm: OnConfirm,
    viewModel: CreatePasswordViewModel,
) {
    val password by rememberFlowWithLifecycle(viewModel.password)
        .collectAsState("")

    val length by rememberFlowWithLifecycle(viewModel.length)
        .collectAsState(CreatePasswordViewModel.defaultLength)

    val hasSpecialCharacters by rememberFlowWithLifecycle(viewModel.hasSpecialCharacters)
        .collectAsState(true)

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 48.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PasswordText(
                modifier = Modifier
                    .weight(1f)
                    .height(72.dp),
                password = password
            )
            Spacer(modifier = Modifier.size(8.dp))
            IconButton(
                onClick = { viewModel.regenerate() }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_proton_arrows_rotate),
                    contentDescription = null
                )
            }
        }

        Divider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.width(112.dp),
                text = stringResource(R.string.character_count, length)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Slider(
                value = length.toFloat(),
                valueRange = CreatePasswordViewModel.lengthRange,
                onValueChange = { viewModel.onLengthChange(it.toInt()) }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.special_characters))
            Switch(
                checked = hasSpecialCharacters,
                onCheckedChange = { viewModel.onHasSpecialCharactersChange(it) }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        ProtonSolidButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(48.dp),
            onClick = { onConfirm(password) }
        ) {
            Text(stringResource(R.string.generate_password_confirm))
        }
    }
}

@Composable
private fun PasswordText(
    modifier: Modifier = Modifier,
    password: String
) {
    val annotatedString = password
        .map {
            val color = when {
                CharacterSet.Digit.value.contains(it) -> ProtonTheme.colors.notificationError
                CharacterSet.Special.value.contains(it) -> ProtonTheme.colors.notificationSuccess
                else -> ProtonTheme.colors.textNorm
            }
            AnnotatedString(it.toString(), SpanStyle(color))
        }
        .reduceOrNull { acc, next -> acc.plus(next) } ?: AnnotatedString("")

    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = annotatedString,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.h6,
        )
    }
}
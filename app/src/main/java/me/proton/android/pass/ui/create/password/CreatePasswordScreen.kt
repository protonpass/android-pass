package me.proton.android.pass.ui.create.password

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import me.proton.android.pass.R
import me.proton.android.pass.ui.shared.ArrowBackIcon
import me.proton.android.pass.ui.shared.TopBarTitleView
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.pass.presentation.components.common.rememberFlowWithLifecycle

internal typealias OnConfirm = (String) -> Unit

@ExperimentalComposeUiApi
@Composable
fun CreatePasswordView(
    onUpClick: () -> Unit,
    onConfirm: OnConfirm,
    viewModel: CreatePasswordViewModel = hiltViewModel()
) {
    val state by rememberFlowWithLifecycle(viewModel.state).collectAsState(initial = viewModel.initialState)

    Scaffold(
        topBar = {
            ProtonTopAppBar(
                title = { TopBarTitleView(R.string.title_create_password) },
                navigationIcon = { ArrowBackIcon(onUpClick = onUpClick) },
                actions = {},
            )
        }) { padding ->

        Box(modifier = Modifier.padding(padding)) {
            CreatePasswordViewContent(
                state = state,
                onConfirm = onConfirm,
                onLengthChange = { viewModel.onLengthChange(it) },
                onRegenerateClick = { viewModel.regenerate() },
                onSpecialCharactersChange = { viewModel.onHasSpecialCharactersChange(it) }
            )
        }
    }
}

@Composable
private fun CreatePasswordViewContent(
    state: CreatePasswordViewModel.ViewState,
    onConfirm: OnConfirm,
    onSpecialCharactersChange: (Boolean) -> Unit,
    onLengthChange: (Int) -> Unit,
    onRegenerateClick: () -> Unit,
) {

    Column {
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
                password = state.password
            )
            Spacer(modifier = Modifier.size(8.dp))
            IconButton(
                onClick = { onRegenerateClick() }
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
                text = stringResource(R.string.character_count, state.length)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Slider(
                value = state.length.toFloat(),
                valueRange = CreatePasswordViewModel.LENGTH_RANGE,
                onValueChange = { onLengthChange(it.toInt()) }
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
                checked = state.hasSpecialCharacters,
                onCheckedChange = { onSpecialCharactersChange(it) }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        ProtonSolidButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(48.dp),
            onClick = { onConfirm(state.password) }
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
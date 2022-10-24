package me.proton.pass.presentation.create.password

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.android.pass.ui.shared.ArrowBackIcon
import me.proton.android.pass.ui.shared.TopBarTitleView
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.previewproviders.CreatePasswordStatePreviewProvider

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CreatePasswordContent(
    modifier: Modifier = Modifier,
    state: CreatePasswordUiState,
    onUpClick: () -> Unit,
    onLengthChange: (Int) -> Unit,
    onRegenerateClick: () -> Unit,
    onHasSpecialCharactersChange: (Boolean) -> Unit,
    onConfirm: (String) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            ProtonTopAppBar(
                title = { TopBarTitleView(R.string.title_create_password) },
                navigationIcon = { ArrowBackIcon(onUpClick = onUpClick) },
                actions = {}
            )
        }
    ) { padding ->

        Column(modifier = Modifier.padding(padding)) {
            CreatePasswordViewContent(
                state = state,
                onLengthChange = onLengthChange,
                onRegenerateClick = onRegenerateClick,
                onSpecialCharactersChange = onHasSpecialCharactersChange
            )

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
}

@Preview(showBackground = true)
@Composable
fun Preview_CreatePasswordContent(
    @PreviewParameter(CreatePasswordStatePreviewProvider::class) state: CreatePasswordUiState
) {
    ProtonTheme {
        CreatePasswordContent(
            state = state,
            onUpClick = {},
            onLengthChange = {},
            onRegenerateClick = {},
            onHasSpecialCharactersChange = {},
            onConfirm = {}
        )
    }
}

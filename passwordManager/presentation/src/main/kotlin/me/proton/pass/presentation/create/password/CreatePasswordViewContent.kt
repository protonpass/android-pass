package me.proton.pass.presentation.create.password

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.previewproviders.CreatePasswordStatePreviewProvider

@Composable
internal fun CreatePasswordViewContent(
    modifier: Modifier = Modifier,
    state: CreatePasswordUiState,
    onSpecialCharactersChange: (Boolean) -> Unit,
    onLengthChange: (Int) -> Unit,
    onRegenerateClick: () -> Unit
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
                    painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_arrows_rotate),
                    contentDescription = null
                )
            }
        }

        Divider()

        Row(
            modifier = Modifier.fillMaxWidth(),
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
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.special_characters))
            Switch(
                checked = state.hasSpecialCharacters,
                onCheckedChange = { onSpecialCharactersChange(it) }
            )
        }
    }
}

@Preview
@Composable
fun CreatePasswordViewContentPreview(
    @PreviewParameter(CreatePasswordStatePreviewProvider::class) state: CreatePasswordUiState
) {
    ProtonTheme {
        Surface {
            CreatePasswordViewContent(
                state = state,
                onSpecialCharactersChange = {},
                onLengthChange = {},
                onRegenerateClick = {}
            )
        }
    }
}

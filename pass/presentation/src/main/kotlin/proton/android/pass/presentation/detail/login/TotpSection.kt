package proton.android.pass.presentation.detail.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.proton.pass.presentation.R
import proton.android.pass.composecomponents.impl.container.RoundedCornersContainer
import proton.android.pass.presentation.detail.DetailSectionTitle

@Composable
fun TotpSection(
    modifier: Modifier = Modifier,
    state: TotpUiState,
    onCopyTotpClick: (String) -> Unit
) {
    RoundedCornersContainer(
        modifier = modifier.fillMaxWidth(),
        onClick = { onCopyTotpClick(state.code) }
    ) {
        Column {
            DetailSectionTitle(text = stringResource(R.string.totp_section_title))
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = state.code)
                Spacer(modifier = Modifier.width(20.dp))
                Text(
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End,
                    text = state.remainingSeconds.toString()
                )
                Spacer(modifier = Modifier.width(20.dp))
            }
        }
    }
}

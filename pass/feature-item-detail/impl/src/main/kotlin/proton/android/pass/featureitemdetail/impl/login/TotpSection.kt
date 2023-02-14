package proton.android.pass.featureitemdetail.impl.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassColors
import proton.android.pass.featureitemdetail.impl.R
import proton.android.pass.featureitemdetail.impl.SectionSubtitle
import proton.android.pass.featureitemdetail.impl.SectionTitle

@Composable
fun TotpRow(
    modifier: Modifier = Modifier,
    state: TotpUiState,
    onCopyTotpClick: (String) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCopyTotpClick(state.code) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_lock),
            contentDescription = stringResource(R.string.totp_icon_content_description),
            tint = PassColors.PurpleAccent
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            SectionTitle(text = stringResource(R.string.totp_section_title))
            Spacer(modifier = Modifier.height(8.dp))
            val half = state.code.length / 2
            SectionSubtitle(
                text = state.code.take(half) + "•" + state.code.takeLast(half)
            )
        }
        Text(
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
            text = state.remainingSeconds.toString()
        )
    }
}

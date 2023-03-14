package proton.android.pass.featureprofile.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallStrong

@Composable
fun ProfileContent(
    modifier: Modifier = Modifier,
    onAccountClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(R.string.profile_manage_profile),
            style = ProtonTheme.typography.defaultSmallStrong
        )
        AccountSettingsSection(onAccountClick = onAccountClick, onSettingsClick = onSettingsClick)
        HelpCenterSection()
    }
}


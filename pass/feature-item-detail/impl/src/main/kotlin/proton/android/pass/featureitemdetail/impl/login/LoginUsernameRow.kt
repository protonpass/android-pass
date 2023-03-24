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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.asAnnotatedString
import proton.android.pass.composecomponents.impl.item.SectionTitle
import proton.android.pass.featureitemdetail.impl.R
import proton.android.pass.featureitemdetail.impl.common.SectionSubtitle

@Composable
fun LoginUsernameRow(
    modifier: Modifier = Modifier,
    username: String,
    onUsernameClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onUsernameClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_user),
            contentDescription = stringResource(R.string.username_icon_content_description),
            tint = PassTheme.colors.loginInteractionNormMajor1
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            SectionTitle(text = stringResource(R.string.field_username))
            Spacer(modifier = Modifier.height(8.dp))
            SectionSubtitle(text = username.asAnnotatedString())
        }
    }
}

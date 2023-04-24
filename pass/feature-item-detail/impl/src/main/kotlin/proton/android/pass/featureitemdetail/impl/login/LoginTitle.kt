package proton.android.pass.featureitemdetail.impl.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.item.icon.LoginIcon
import proton.android.pass.featureitemdetail.impl.common.ItemTitleInput
import proton.android.pass.featureitemdetail.impl.common.ItemTitleText
import proton.android.pass.featureitemdetail.impl.common.ThemeItemTitleProvider
import proton.android.pass.featureitemdetail.impl.common.VaultNameSubtitle
import proton.pass.domain.Vault

@Composable
fun LoginTitle(
    modifier: Modifier = Modifier,
    title: String,
    website: String?,
    packageName: String?,
    vault: Vault?
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LoginIcon(
            size = 60,
            shape = PassTheme.shapes.squircleMediumLargeShape,
            text = title,
            website = website,
            packageName = packageName
        )
        Column(modifier = Modifier.fillMaxWidth()) {
            ItemTitleText(text = title)
            VaultNameSubtitle(vault = vault)
        }
    }
}

@Preview
@Composable
fun LoginTitlePreview(
    @PreviewParameter(ThemeItemTitleProvider::class) input: Pair<Boolean, ItemTitleInput>
) {
    PassTheme(isDark = input.first) {
        Surface {
            LoginTitle(
                title = input.second.title,
                website = null,
                packageName = null,
                vault = input.second.vault
            )
        }
    }
}

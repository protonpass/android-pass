package proton.android.pass.featureitemdetail.impl.alias

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.item.icon.AliasIcon
import proton.android.pass.featureitemdetail.impl.common.ItemTitleInput
import proton.android.pass.featureitemdetail.impl.common.ItemTitleText
import proton.android.pass.featureitemdetail.impl.common.ThemeItemTitleProvider
import proton.android.pass.featureitemdetail.impl.common.VaultNameSubtitle
import proton.pass.domain.Vault

@Composable
fun AliasTitle(
    modifier: Modifier = Modifier,
    title: String,
    vault: Vault?
) {
    Row(
        modifier = modifier.height(75.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AliasIcon(size = 60, shape = PassTheme.shapes.squircleMediumLargeShape)
        Column(modifier = Modifier.fillMaxWidth()) {
            ItemTitleText(text = title)
            VaultNameSubtitle(vault = vault)
        }
    }
}

@Preview
@Composable
fun AliasTitlePreview(
    @PreviewParameter(ThemeItemTitleProvider::class) input: Pair<Boolean, ItemTitleInput>
) {
    PassTheme(isDark = input.first) {
        Surface {
            AliasTitle(
                title = input.second.title,
                vault = input.second.vault
            )
        }
    }
}
